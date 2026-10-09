package com.coordenapleito.api.websocket;

import com.coordenapleito.api.dto.CoordenadorVinculoResumoModel;
import com.coordenapleito.domain.service.coordenador.CoordenadorVinculoResumoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoordenadorVinculoWsHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessoes = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;
    private final CoordenadorVinculoResumoService resumoService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessoes.add(session);
        log.info("[graficos-ws] conectado id={} sessoes={}", session.getId(), sessoes.size());
        try {
            enviar(session, resumoService.resumir());
        } catch (Exception e) {
            log.warn("[graficos-ws] falha ao enviar snapshot: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessoes.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessoes.remove(session);
    }

    public void broadcast() {
        if (sessoes.isEmpty()) {
            log.debug("[graficos-ws] broadcast ignorado: nenhuma sessão");
            return;
        }
        log.info("[graficos-ws] broadcast para {} sessão(ões)", sessoes.size());
        CoordenadorVinculoResumoModel resumo = resumoService.resumir();
        for (WebSocketSession session : sessoes) {
            enviar(session, resumo);
        }
    }

    private void enviar(WebSocketSession session, CoordenadorVinculoResumoModel resumo) {
        if (!session.isOpen()) {
            sessoes.remove(session);
            return;
        }
        try {
            TextMessage mensagem = new TextMessage(objectMapper.writeValueAsString(resumo));
            synchronized (session) {
                session.sendMessage(mensagem);
            }
        } catch (Exception e) {
            log.warn("[graficos-ws] falha ao enviar id={}: {}", session.getId(), e.getMessage());
            sessoes.remove(session);
        }
    }
}
