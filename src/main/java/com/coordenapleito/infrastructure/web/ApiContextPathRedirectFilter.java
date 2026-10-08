package com.coordenapleito.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Compatibilidade para clientes que chamam endpoints sem o context-path (/coordenapleito-api).
 * Redireciona (307) para a URL correta preservando método e query string.
 */
@Component
@Order(10)
public class ApiContextPathRedirectFilter extends OncePerRequestFilter {

    private static final Set<String> ROOTS_COMPATIVEIS = Set.of(
            "/dashboard",
            "/mensagens"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();

        // Sem context path configurado: nada para corrigir.
        if (contextPath == null || contextPath.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Se já veio com context-path correto, segue normalmente.
        if (uri.startsWith(contextPath + "/") || uri.equals(contextPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Redireciona apenas os roots de API conhecidos (evita interferir em outras rotas).
        boolean shouldRedirect = ROOTS_COMPATIVEIS.stream()
                .anyMatch(root -> uri.equals(root) || uri.startsWith(root + "/"));
        if (!shouldRedirect) {
            filterChain.doFilter(request, response);
            return;
        }

        String target = contextPath + uri;
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            target = target + "?" + query;
        }

        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT); // 307
        response.setHeader("Location", target);
    }
}
