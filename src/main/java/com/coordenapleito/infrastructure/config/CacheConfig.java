package com.coordenapleito.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_VAPT_VUPT_DASHBOARD = "vaptVuptDashboardResumo";
    public static final String CACHE_VAPT_VUPT_LISTAGEM = "vaptVuptListagem";
    public static final String CACHE_CASA_CIDADAO_DASHBOARD = "casaCidadaoDashboardResumo";
    public static final String CACHE_CAMINHAO_CIDADAO_DASHBOARD = "caminhaoCidadaoDashboardResumo";
    public static final String CACHE_SQP_DASHBOARD = "sqpDashboardResumo";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                CACHE_VAPT_VUPT_DASHBOARD,
                CACHE_VAPT_VUPT_LISTAGEM,
                CACHE_CASA_CIDADAO_DASHBOARD,
                CACHE_CAMINHAO_CIDADAO_DASHBOARD,
                CACHE_SQP_DASHBOARD);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(8));
        return manager;
    }
}
