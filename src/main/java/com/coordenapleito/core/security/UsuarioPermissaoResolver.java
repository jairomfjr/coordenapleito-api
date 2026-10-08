package com.coordenapleito.core.security;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolve permissões funcionais ({@code recurso.acao}) do usuário a partir dos grupos ativos.
 * Ignora vínculos legados {@code ROLE_*} — apenas {@link Permissao#getChave()} entra no JWT.
 */
@Component
public class UsuarioPermissaoResolver {

    public Set<String> resolverChaves(Usuario usuario) {
        if (usuario == null || usuario.getGrupos() == null) {
            return Set.of();
        }
        Set<String> chaves = new LinkedHashSet<>();
        for (Grupo grupo : usuario.getGrupos()) {
            if (grupo == null || !Boolean.TRUE.equals(grupo.getAtivo()) || grupo.getPermissoes() == null) {
                continue;
            }
            for (Permissao p : grupo.getPermissoes()) {
                if (p == null || !Boolean.TRUE.equals(p.getAtivo())) {
                    continue;
                }
                if (p.getChave() == null || p.getChave().isBlank()) {
                    continue;
                }
                String chave = p.getChave().trim();
                if (chave.startsWith("ROLE_")) {
                    continue;
                }
                chaves.add(chave);
            }
        }
        return chaves;
    }

    public Set<String> resolverChaves(Collection<Grupo> grupos) {
        if (grupos == null) {
            return Set.of();
        }
        Usuario u = new Usuario();
        u.setGrupos(grupos.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        return resolverChaves(u);
    }
}
