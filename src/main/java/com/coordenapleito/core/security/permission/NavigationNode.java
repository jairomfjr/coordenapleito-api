package com.coordenapleito.core.security.permission;

import java.util.List;

/**
 * Nó da árvore de permissões alinhado ao menu do Coordenapleito.
 * Grupos têm {@code recurso == null}; folhas de recurso recebem permissões do banco.
 */
public record NavigationNode(
        String id,
        String label,
        String recurso,
        List<NavigationNode> children,
        int ordem) {

    public boolean isGrupo() {
        return recurso == null || recurso.isBlank();
    }
}
