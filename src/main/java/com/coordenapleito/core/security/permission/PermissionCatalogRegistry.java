package com.coordenapleito.core.security.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Catálogo canônico de permissões funcionais do Coordenapleito.
 * Fonte única do seed da migration V2 e das constantes {@link com.coordenapleito.core.security.Permissoes}.
 */
public final class PermissionCatalogRegistry {

    private static final List<PermissionDefinition> ALL = buildAll();

    private PermissionCatalogRegistry() {}

    public static List<PermissionDefinition> getAll() {
        return Collections.unmodifiableList(ALL);
    }

    private static List<PermissionDefinition> buildAll() {
        List<PermissionDefinition> list = new ArrayList<>();
        int order = 0;

        readOnlyUi(list, "geral", "inicio", "Página inicial", order);
        order += 20;

        crudUi(list, "administracao", "usuario", "Usuários", order);
        order += 20;
        crudUi(list, "administracao", "grupo", "Grupos", order);
        order += 20;
        list.add(def("grupo.gerenciar-permissoes", "administracao", "grupo", "gerenciar-permissoes", "Gerenciar permissões do grupo", order++));
        order += 10;
        readOnlyUi(list, "administracao", "permissao", "Permissões", order);
        order += 20;
        crudUi(list, "pleito", "local-votacao", "Locais de votação", order);
        order += 20;
        list.add(def(
                "local-votacao.bloquear-campos",
                "pleito",
                "local-votacao",
                "bloquear-campos",
                "Bloquear campos — Locais de votação (exceto coordenadores)",
                order++));
        order += 10;
        crudUi(list, "pleito", "coordenador", "Coordenadores", order);
        order += 20;
        list.add(def("usuario.alterar-senha", "administracao", "usuario", "alterar-senha", "Alterar senha de usuário", order++));
        list.add(def(
                "relatorio-dashboard.gerar-qualificacao",
                "relatorios",
                "relatorio-dashboard",
                "gerar-qualificacao",
                "Gerar PDF — Dashboard Qualificação",
                order++));

        return list;
    }

    private static void crudUi(List<PermissionDefinition> list, String modulo, String recurso, String label, int baseOrder) {
        crudUiModalEquipamento(list, modulo, recurso, label, baseOrder, Map.of());
    }

    private static void crudUiModalEquipamento(
            List<PermissionDefinition> list,
            String modulo,
            String recurso,
            String label,
            int baseOrder,
            Map<String, String> rotulosAcao) {
        int o = baseOrder;
        list.add(def(recurso + ".menu", modulo, recurso, "menu", "Menu — " + label, o++));
        list.add(def(recurso + ".pagina", modulo, recurso, "pagina", "Página — " + label, o++));
        list.add(def(recurso + ".listar", modulo, recurso, "listar", "Listar — " + label, o++));
        list.add(def(recurso + ".visualizar", modulo, recurso, "visualizar", "Visualizar — " + label, o++));
        list.add(def(
                recurso + ".criar",
                modulo,
                recurso,
                "criar",
                rotulosAcao.getOrDefault("criar", "Criar — " + label),
                o++));
        list.add(def(
                recurso + ".editar",
                modulo,
                recurso,
                "editar",
                rotulosAcao.getOrDefault("editar", "Editar — " + label),
                o++));
        list.add(def(
                recurso + ".excluir",
                modulo,
                recurso,
                "excluir",
                rotulosAcao.getOrDefault("excluir", "Excluir — " + label),
                o++));
    }

    private static void readOnlyUi(List<PermissionDefinition> list, String modulo, String recurso, String label, int baseOrder) {
        int o = baseOrder;
        list.add(def(recurso + ".menu", modulo, recurso, "menu", "Menu — " + label, o++));
        list.add(def(recurso + ".pagina", modulo, recurso, "pagina", "Página — " + label, o++));
        list.add(def(recurso + ".listar", modulo, recurso, "listar", "Listar — " + label, o++));
        list.add(def(recurso + ".visualizar", modulo, recurso, "visualizar", "Visualizar — " + label, o++));
    }

    private static PermissionDefinition def(
            String chave, String modulo, String recurso, String acao, String descricao, int ordem) {
        return new PermissionDefinition(chave, modulo, recurso, acao, descricao, ordem, true);
    }
}
