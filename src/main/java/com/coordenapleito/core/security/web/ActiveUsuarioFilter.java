package com.coordenapleito.core.security.web;

import com.coordenapleito.api.exceptionhandler.Problem;
import com.coordenapleito.api.exceptionhandler.ProblemType;
import com.coordenapleito.core.security.UsuarioSecurityMessages;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.infrastructure.util.CpfUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Bloqueia uso da API com JWT de usuário {@code ativo = false} (ex.: desativado após o login).
 */
@Component
@RequiredArgsConstructor
public class ActiveUsuarioFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String cpf = extrairCpf(auth);
        if (cpf == null) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean ativo = usuarioRepository
                .findByCpf(CpfUtils.normalizar(cpf))
                .map(Usuario::isContaAtiva)
                .orElse(false);

        if (ativo) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.clearContext();
        response.addHeader(HttpHeaders.SET_COOKIE, cookieLogout().toString());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                Problem.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .timestamp(OffsetDateTime.now())
                        .type(ProblemType.ACESSO_NEGADO.getUri())
                        .title(ProblemType.ACESSO_NEGADO.getTitle())
                        .detail(UsuarioSecurityMessages.USUARIO_INATIVO)
                        .userMessage(UsuarioSecurityMessages.USUARIO_INATIVO)
                        .build());
    }

    private static String extrairCpf(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt != null ? jwt.getSubject() : null;
        }
        return auth.getName();
    }

    private ResponseCookie cookieLogout() {
        return ResponseCookie.from(JwtCookieToBearerFilter.COOKIE_ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

}
