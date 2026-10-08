package com.coordenapleito.core.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Substitui o header {@code Authorization} por {@code Bearer <token>} válido (query/cookie/SSE).
 */
public class JwtCookieBearerRequestWrapper extends HttpServletRequestWrapper {

    private final String authorizationHeader;

    public JwtCookieBearerRequestWrapper(HttpServletRequest request, String bearerToken) {
        super(request);
        this.authorizationHeader = "Bearer " + bearerToken;
    }

    @Override
    public String getHeader(String name) {
        if ("Authorization".equalsIgnoreCase(name)) {
            return authorizationHeader;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if ("Authorization".equalsIgnoreCase(name)) {
            return Collections.enumeration(List.of(authorizationHeader));
        }
        return super.getHeaders(name);
    }
}
