package com.coordenapleito.api.websocket;

import com.coordenapleito.domain.event.VinculosCoordenadorAtualizadosEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VinculosCoordenadorWsListener {

    private final CoordenadorVinculoWsHandler handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(VinculosCoordenadorAtualizadosEvent event) {
        log.info("[graficos-ws] evento de vínculo após commit");
        handler.broadcast();
    }
}
