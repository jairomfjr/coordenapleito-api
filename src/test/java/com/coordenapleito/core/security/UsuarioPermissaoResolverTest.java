package com.coordenapleito.core.security;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioPermissaoResolverTest {

    private final UsuarioPermissaoResolver resolver = new UsuarioPermissaoResolver();

    @Test
    void resolveChavesDistintasDeGruposAtivos() {
        Permissao p1 = permissao("usuario.listar");
        Permissao p2 = permissao("grupo.listar");
        Permissao inativa = permissao("usuario.excluir");
        inativa.setAtivo(false);

        Grupo g1 = new Grupo();
        g1.setPermissoes(new LinkedHashSet<>(List.of(p1, p2)));
        Grupo g2 = new Grupo();
        g2.setPermissoes(new LinkedHashSet<>(List.of(p1, inativa)));

        Usuario u = new Usuario();
        u.setGrupos(List.of(g1, g2));

        Set<String> chaves = resolver.resolverChaves(u);
        assertEquals(2, chaves.size());
        assertTrue(chaves.contains("usuario.listar"));
        assertTrue(chaves.contains("grupo.listar"));
    }

    @Test
    void ignoraPermissaoLegadaRole() {
        Permissao legado = new Permissao();
        legado.setNome("ROLE_ANALISTA");
        legado.setChave("ROLE_ANALISTA");
        legado.setAtivo(true);

        Permissao funcional = permissao("equipamento.listar");

        Grupo g = new Grupo();
        g.setPermissoes(new LinkedHashSet<>(List.of(legado, funcional)));

        Usuario u = new Usuario();
        u.setGrupos(List.of(g));

        Set<String> chaves = resolver.resolverChaves(u);
        assertEquals(1, chaves.size());
        assertTrue(chaves.contains("equipamento.listar"));
    }

    private static Permissao permissao(String chave) {
        Permissao p = new Permissao();
        p.setChave(chave);
        p.setAtivo(true);
        return p;
    }
}
