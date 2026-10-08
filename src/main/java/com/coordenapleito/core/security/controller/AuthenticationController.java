package com.coordenapleito.core.security.controller;

import com.coordenapleito.core.security.AuthenticationModel;
import com.coordenapleito.core.security.service.AuthenticationService;
import com.coordenapleito.core.security.web.JwtCookieToBearerFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private static final long JWT_EXPIRY_SECONDS = 36000L;

    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    /**
     * SPA usa {@code accessToken} no JSON + Bearer; cookie HttpOnly é opcional (legado/produção).
     */
    @Value("${app.security.auth.http-only-cookie:false}")
    private boolean httpOnlyCookie;

    /**
     * Path do cookie alinhado ao {@code server.servlet.context-path} (ex.: /coordenapleito-api).
     */
    @Value("${server.servlet.context-path:/}")
    private String servletContextPath;

    /**
     * Login: o SPA envia {@code Authorization: Basic base64(cpf:senha)}.
     * Sem {@code httpBasic()} no {@code SecurityFilterChain}, o Spring não preenche {@link Authentication}
     * automaticamente; autenticamos aqui com {@link AuthenticationManager} (mesmo {@link org.springframework.security.core.userdetails.UserDetailsService}).
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationModel> authenticate(HttpServletRequest request) {
        Authentication auth = authenticateFromBasicHeader(request);
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthenticationModel body = authenticationService.authenticate(auth);
        return okResponse(body);
    }

    /**
     * Decodifica {@code Authorization: Basic ...} e delega ao {@link AuthenticationManager}.
     */
    private Authentication authenticateFromBasicHeader(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            String base64Token = header.substring(6).trim();
            String decoded = new String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            String username = colon < 0 ? decoded : decoded.substring(0, colon);
            String password = colon < 0 ? "" : decoded.substring(colon + 1);
            UsernamePasswordAuthenticationToken token =
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password);
            return authenticationManager.authenticate(token);
        } catch (DisabledException e) {
            throw e;
        } catch (IllegalArgumentException | AuthenticationException e) {
            return null;
        }
    }

    private ResponseEntity<AuthenticationModel> okResponse(AuthenticationModel body) {
        // Sempre expira cookies legados em path do context-path (JWT antigo com permissões estourava HTTP 431).
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAccessTokenCookie(normalizeCookiePath(servletContextPath)).toString());
        if (!httpOnlyCookie) {
            // SPA usa só Bearer; limpa também path "/" caso cookie tenha sido gravado assim.
            return builder
                    .header(HttpHeaders.SET_COOKIE, clearAccessTokenCookie("/").toString())
                    .body(body);
        }
        ResponseCookie cookie = ResponseCookie.from(JwtCookieToBearerFilter.COOKIE_ACCESS_TOKEN, body.getAccessToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(JWT_EXPIRY_SECONDS))
                .sameSite("Lax")
                .build();
        return builder
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    /**
     * Hidrata sessão no SPA: cookie HttpOnly enviado automaticamente; não depende de token no localStorage.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthenticationModel> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ResponseEntity<AuthenticationModel> response = authenticationService.checkToken(jwt.getTokenValue());
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return response;
        }
        return okResponse(response.getBody());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearAccessTokenCookie("/").toString())
                .header(HttpHeaders.SET_COOKIE, clearAccessTokenCookie(normalizeCookiePath(servletContextPath)).toString())
                .build();
    }

    private ResponseCookie clearAccessTokenCookie(String path) {
        return ResponseCookie.from(JwtCookieToBearerFilter.COOKIE_ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(path)
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    @PostMapping("/check_token")
    public ResponseEntity<AuthenticationModel> checkToken(@RequestParam("token") String token) {
        return authenticationService.checkToken(token);
    }

    private static String normalizeCookiePath(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "/";
        }
        String p = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
        return p.replaceAll("/+$", "");
    }
}
