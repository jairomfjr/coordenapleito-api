package com.coordenapleito.infrastructure.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import com.coordenapleito.domain.service.EnvioEmailService;

@Slf4j
public class FakeEnvioEmailService implements EnvioEmailService {

    @Autowired
    private ProcessadorEmailTemplate processadorEmailTemplate;
    
    @Override
    public void enviar(Mensagem mensagem) {
        String corpo = processadorEmailTemplate.processar(mensagem);
        log.info("[FAKE E-MAIL] Para: {}\n{}", mensagem.getDestinatarios(), corpo);
    }
}