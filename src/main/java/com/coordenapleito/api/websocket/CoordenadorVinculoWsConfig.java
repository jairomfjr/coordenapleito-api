package com.coordenapleito.api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class CoordenadorVinculoWsConfig implements WebSocketConfigurer {

    private final CoordenadorVinculoWsHandler handler;
    private final JwtHandshakeInterceptor interceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/coordenadores-vinculos")
                .addInterceptors(interceptor)
                .setAllowedOriginPatterns("*");
    }
}
