package com.coordenapleito.core.security.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estrutura de navegação canônica (espelha o menu do frontend).
 * Fonte para {@link com.coordenapleito.domain.service.permissao.ListPermissaoArvoreService}.
 */
public final class NavigationCatalogRegistry {

    private static final List<NavigationNode> ROOTS = buildRoots();

    private NavigationCatalogRegistry() {}

    public static List<NavigationNode> getRoots() {
        return Collections.unmodifiableList(ROOTS);
    }

    private static List<NavigationNode> buildRoots() {
        List<NavigationNode> roots = new ArrayList<>();
        int o = 0;

        roots.add(recurso("inicio", "Página inicial", o++));

        roots.add(grupo("administracao", "Administração", o++, List.of(
                recurso("usuario", "Usuários", 0),
                recurso("grupo", "Grupos", 1),
                recurso("permissao", "Permissões", 2))));

        return roots;
    }

    private static NavigationNode grupo(String id, String label, int ordem, List<NavigationNode> filhos) {
        return new NavigationNode(id, label, null, filhos, ordem);
    }

    private static NavigationNode recurso(String recurso, String label, int ordem) {
        return new NavigationNode(recurso, label, recurso, List.of(), ordem);
    }

    private static NavigationNode recurso(String recurso, String label, int ordem, List<NavigationNode> filhos) {
        return new NavigationNode(recurso, label, recurso, filhos, ordem);
    }
}
