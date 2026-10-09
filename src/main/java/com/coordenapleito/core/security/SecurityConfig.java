package com.coordenapleito.core.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.coordenapleito.core.security.web.ActiveUsuarioFilter;
import com.coordenapleito.core.security.web.JwtCookieToBearerFilter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${jwt.public.key}")
    private RSAPublicKey key;
    @Value("${jwt.private.key}")
    private RSAPrivateKey priv;

    @Bean
    JwtCookieToBearerFilter jwtCookieToBearerFilter(JwtDecoder jwtDecoder) {
        return new JwtCookieToBearerFilter(jwtDecoder);
    }

    /**
     * Usado em {@code AuthenticationController} para validar login via cabeçalho
     * {@code Authorization: Basic} sem reativar {@code httpBasic()} no filtro global
     * (evita modal nativo do navegador em 401).
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtCookieToBearerFilter jwtCookieToBearerFilter,
            ActiveUsuarioFilter activeUsuarioFilter,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/auth/authenticate").permitAll()
                                .requestMatchers("/auth/check_token").permitAll()
                                .requestMatchers("/auth/logout").permitAll()
                                .requestMatchers("/usuarios/recuperar-senha").permitAll()
                                .requestMatchers("/publico/**").permitAll()
                                .requestMatchers("/docs/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .anyRequest().authenticated())
                // Não usar httpBasic(): isso envia WWW-Authenticate: Basic em 401 e o navegador abre
                // o modal nativo de login ao consumir a API a partir do SPA (Next.js em :3000 → API :8080).
                // Autenticação é JWT (Bearer + cookie HttpOnly via JwtCookieToBearerFilter).
                .oauth2ResourceServer(conf -> conf
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterBefore(jwtCookieToBearerFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(activeUsuarioFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    JwtDecoder JwtDecoder(){
        return NimbusJwtDecoder.withPublicKey(key).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    /**
     * Converte o claim "authorities" do JWT em GrantedAuthority para @PreAuthorize/hasRole.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(
            CoordenapleitoJwtGrantedAuthoritiesConverter coordenapleitoJwtGrantedAuthoritiesConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(coordenapleitoJwtGrantedAuthoritiesConverter);
        return converter;
    }
}