-- =============================================================================
-- Migração: remover grupo ADMIN e atribuir usuários ao grupo Administrador
-- Execute após garantir que o grupo "Administrador" e ROLE_ADMINISTRADOR existam
-- (ex.: script_insert_grupos_permissoes.sql já foi executado).
-- =============================================================================

DO $$
DECLARE
  v_grupo_admin_id    BIGINT;
  v_grupo_adm_id     BIGINT;
  v_usuario_id_rec   BIGINT;
BEGIN
  -- 1. Obter ID do grupo Administrador (destino)
  SELECT id INTO v_grupo_adm_id FROM grupo WHERE nome = 'Administrador' LIMIT 1;
  IF v_grupo_adm_id IS NULL THEN
    RAISE EXCEPTION 'Grupo "Administrador" não encontrado. Execute script_insert_grupos_permissoes.sql antes.';
  END IF;

  -- 2. Para cada grupo cujo nome é 'ADMIN' ou 'Admin'
  FOR v_grupo_admin_id IN
    SELECT id FROM grupo WHERE nome IN ('ADMIN', 'Admin')
  LOOP
    -- 2.1 Inserir usuario_grupo (usuario_id, grupo_id) para Administrador nos usuários que tinham ADMIN
    INSERT INTO usuario_grupo (usuario_id, grupo_id)
    SELECT ug.usuario_id, v_grupo_adm_id
    FROM usuario_grupo ug
    WHERE ug.grupo_id = v_grupo_admin_id
      AND NOT EXISTS (
        SELECT 1 FROM usuario_grupo ug2
        WHERE ug2.usuario_id = ug.usuario_id AND ug2.grupo_id = v_grupo_adm_id
      );

    -- 2.2 Remover vínculos usuario_grupo com o grupo ADMIN/Admin
    DELETE FROM usuario_grupo WHERE grupo_id = v_grupo_admin_id;

    -- 2.3 Remover vínculos grupo_permissao do grupo ADMIN/Admin
    DELETE FROM grupo_permissao WHERE grupo_id = v_grupo_admin_id;

    -- 2.4 Remover o grupo ADMIN/Admin
    DELETE FROM grupo WHERE id = v_grupo_admin_id;

    RAISE NOTICE 'Grupo com id % (ADMIN/Admin) removido; usuários migrados para Administrador.', v_grupo_admin_id;
  END LOOP;

  -- 3. Remover permissão ROLE_ADMIN se existir (grupos ADMIN/Admin já foram removidos acima)
  DELETE FROM permissao WHERE nome = 'ROLE_ADMIN';
END $$;
