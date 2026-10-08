package com.coordenapleito.infrastructure.config;

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Garante regras de validação do Flyway em todos os ambientes (incl. K8s production),
 * mesmo quando o histórico do banco contém migrações legadas ausentes no artefato atual.
 */
@Configuration
public class CoordenapleitoFlywayConfig {

    @Bean
    FlywayConfigurationCustomizer coordenapleitoFlywayCustomizer() {
        return configuration -> configuration
                .locations(
                        "classpath:db/migration",
                        "classpath:com/coordenapleito/infrastructure/flyway")
                .ignoreMigrationPatterns("*:missing");
    }
}
