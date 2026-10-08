package com.coordenapleito.core.security.permission;

/**
 * Definição imutável de uma permissão funcional (chave {@code recurso.acao}).
 */
public record PermissionDefinition(
        String chave,
        String modulo,
        String recurso,
        String acao,
        String descricao,
        int ordem,
        boolean sistema) {}
