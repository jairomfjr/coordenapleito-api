package com.coordenapleito.infrastructure.flyway;

import com.coordenapleito.core.security.permission.PermissionCatalogRegistry;
import com.coordenapleito.core.security.permission.PermissionDefinition;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * Insere o catálogo funcional atual, de forma idempotente, a partir de {@link PermissionCatalogRegistry}.
 */
public class V2__seed_permissoes_funcionais extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        for (PermissionDefinition def : PermissionCatalogRegistry.getAll()) {
            insertIfAbsent(conn, def);
        }
    }

    private static void insertIfAbsent(Connection conn, PermissionDefinition def) throws Exception {
        String check = "SELECT id FROM permissao WHERE chave = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, def.chave());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String sql = """
                INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo,
                    chave, modulo, recurso, acao, ordem, sistema)
                VALUES (nextval('permissao_id_seq'), ?, NOW(), NOW(), ?, ?, TRUE, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setString(2, def.chave());
            ps.setString(3, def.descricao());
            ps.setString(4, def.chave());
            ps.setString(5, def.modulo());
            ps.setString(6, def.recurso());
            ps.setString(7, def.acao());
            ps.setInt(8, def.ordem());
            ps.setBoolean(9, def.sistema());
            ps.executeUpdate();
        }
    }
}
