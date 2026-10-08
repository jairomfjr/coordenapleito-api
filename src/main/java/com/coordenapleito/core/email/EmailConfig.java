package com.coordenapleito.core.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.coordenapleito.domain.service.EnvioEmailService;
import com.coordenapleito.infrastructure.service.email.FakeEnvioEmailService;
import com.coordenapleito.infrastructure.service.email.SandboxEnvioEmailService;
import com.coordenapleito.infrastructure.service.email.SmtpEnvioEmailService;

public class EmailConfig {

    @Autowired
    private EmailProperties emailProperties;

    @Bean
    public EnvioEmailService envioEmailService() {
            switch (emailProperties.getImpl()) {
            case FAKE:
                return new FakeEnvioEmailService();
            case SMTP:
                return new SmtpEnvioEmailService();
            case SANDBOX:
                return new SandboxEnvioEmailService();
            default:
                return null;
        }
    }
}