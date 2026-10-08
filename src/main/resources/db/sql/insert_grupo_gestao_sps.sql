-- =============================================================================
-- Perfil Gestão SPS: cadastros operacionais (dashboard, dados básicos restritos,
-- equipamentos, coordenações, cidadãos). Idempotente.
-- =============================================================================

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_GESTÃO_SPS', 'Permissão do perfil Gestão SPS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_GESTÃO_SPS');

INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Gestão SPS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Gestão SPS');

INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM grupo g
CROSS JOIN permissao p
WHERE g.nome = 'Gestão SPS' AND p.nome = 'ROLE_GESTÃO_SPS'
  AND NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
  );
