package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.domain.event.VinculosCoordenadorAtualizadosEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VinculosCoordenadorEventos {

    private final ApplicationEventPublisher publisher;

    public void notificar() {
        publisher.publishEvent(new VinculosCoordenadorAtualizadosEvent());
    }
}
