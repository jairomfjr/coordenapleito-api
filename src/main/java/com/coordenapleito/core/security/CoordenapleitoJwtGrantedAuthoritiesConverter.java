package com.coordenapleito.core.security;

import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.infrastructure.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolve permissões funcionais no banco a partir do {@code sub} do JWT.
 * O token permanece pequeno (sem lista de centenas de chaves no cookie).
 */
@Component
@RequiredArgsConstructor
public class CoordenapleitoJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPermissaoResolver usuarioPermissaoResolver;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String cpf = jwt.getSubject();
        if (cpf == null || cpf.isBlank()) {
            return List.of();
        }
        return usuarioRepository.findByCpfWithGruposAndPermissoes(CpfUtils.normalizar(cpf))
                .map(usuario -> usuarioPermissaoResolver.resolverChaves(usuario).stream()
                        .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}
