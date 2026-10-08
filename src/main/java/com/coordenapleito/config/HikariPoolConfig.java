package com.coordenapleito.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Consolidação Vapt Vupt executa SQL agregado no staging por 20–60s (uma conexão legítima).
 * O Hikari interpreta isso como "leak" se {@code leak-detection-threshold > 0}.
 * Força desligamento em stage/prod mesmo quando o K8s define env com threshold baixo.
 */
@Configuration
@Profile({"stage", "production"})
public class HikariPoolConfig {

    @Bean
    static BeanPostProcessor hikariDesabilitarLeakDetection() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof HikariDataSource dataSource) {
                    dataSource.setLeakDetectionThreshold(0);
                }
                return bean;
            }
        };
    }
}
