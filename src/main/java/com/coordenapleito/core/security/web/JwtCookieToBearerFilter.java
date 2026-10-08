package com.coordenapleito.core.security.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Normaliza autenticação JWT:
 * <ul>
 *   <li>Bearer válido → segue inalterado</li>
 *   <li>Bearer inválido em rotas públicas de auth → remove Authorization (evita 401 do OAuth2)</li>
 *   <li>SSE/query {@code access_token} ou cookie válido → injeta Bearer</li>
 * </ul>
 */
@RequiredArgsConstructor
public class JwtCookieToBearerFilter extends OncePerRequestFilter {

    public static final String COOKIE_ACCESS_TOKEN = "access_token";

    private final JwtDecoder jwtDecoder;

    private static boolean isPublicoRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.contains("/publico/");
    }

    private static boolean isPublicAuthEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        return uri.contains("/auth/authenticate")
                || uri.contains("/auth/check_token")
                || uri.contains("/auth/logout");
    }

    private static String extractBearerToken(HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = auth.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private boolean isValidAccessToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            jwtDecoder.decode(token.trim());
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (isPublicoRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearer = extractBearerToken(request);
        if (bearer != null && isValidAccessToken(bearer)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (bearer != null && isPublicAuthEndpoint(request)) {
            filterChain.doFilter(new AuthorizationStripRequestWrapper(request), response);
            return;
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isBlank() && !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String queryToken = request.getParameter("access_token");
        if (queryToken != null && !queryToken.isBlank() && isValidAccessToken(queryToken)) {
            filterChain.doFilter(new JwtCookieBearerRequestWrapper(request, queryToken.trim()), response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (!COOKIE_ACCESS_TOKEN.equals(c.getName()) || c.getValue() == null || c.getValue().isBlank()) {
                    continue;
                }
                String value = c.getValue().trim();
                if (isValidAccessToken(value)) {
                    filterChain.doFilter(new JwtCookieBearerRequestWrapper(request, value), response);
                    return;
                }
            }
        }

        if (bearer != null) {
            filterChain.doFilter(new AuthorizationStripRequestWrapper(request), response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
