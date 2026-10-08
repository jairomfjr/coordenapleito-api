package com.coordenapleito.infrastructure.service.email;

import freemarker.template.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.coordenapleito.domain.service.EnvioEmailService;

@Component
@RequiredArgsConstructor
public class ProcessadorEmailTemplate {

    private final Configuration freemarkerConfig;

    protected String processar(EnvioEmailService.Mensagem mensagem) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfig.getTemplate(mensagem.getCorpo()), mensagem.getVariaveis());
        } catch (Exception e) {
            throw new EmailException("Não foi possível montar o template do e-mail", e);
        }
    }
}