package com.coordenapleito.api.websocket;

import com.coordenapleito.core.security.CoordenapleitoJwtGrantedAuthoritiesConverter;
import com.coordenapleito.core.security.Permissoes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;
    private final CoordenapleitoJwtGrantedAuthoritiesConverter authoritiesConverter;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = extrairToken(request);
        if (token == null || token.isBlank()) {
            log.warn("[graficos-ws] handshake recusado: token ausente path={}", request.getURI().getPath());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Collection<GrantedAuthority> autoridades = authoritiesConverter.convert(jwt);
            boolean permitido = autoridades != null && autoridades.stream()
                    .anyMatch(a -> Permissoes.Inicio.GRAFICOS_COORDENADORES.equals(a.getAuthority()));
            if (!permitido) {
                log.warn("[graficos-ws] handshake recusado: sem permissão cpf={}", jwt.getSubject());
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return false;
            }
            attributes.put("cpf", jwt.getSubject());
            log.info("[graficos-ws] handshake ok cpf={}", jwt.getSubject());
            return true;
        } catch (Exception e) {
            log.warn("[graficos-ws] handshake recusado: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        if (exception != null) {
            log.warn("[graficos-ws] handshake erro: {}", exception.getMessage());
        }
    }

    private static String extrairToken(ServerHttpRequest request) {
        String fromQuery = parametro(request.getURI().getRawQuery(), "access_token");
        if (fromQuery != null && !fromQuery.isBlank()) {
            return fromQuery;
        }
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return auth.substring(7).trim();
        }
        return null;
    }

    private static String parametro(String rawQuery, String nome) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String chave = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            if (nome.equals(chave)) {
                return URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
