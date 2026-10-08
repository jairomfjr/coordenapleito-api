-- =============================================================================
-- Script: Inserir grupos e permissões (Analista, Secretário, Coordenador, Supervisor, Técnico)
-- Banco: PostgreSQL
-- Uso: Executar após as tabelas grupo, permissao e grupo_permissao existirem.
--      Idempotente: não duplica registros já existentes por nome.
-- =============================================================================

-- Permissões (uma por papel, usada como authority no JWT/Spring Security)
INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_ANALISTA', 'Permissão do perfil Analista', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_ANALISTA');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_SECRETARIO', 'Permissão do perfil Secretário', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_SECRETARIO');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_COORDENADOR', 'Permissão do perfil Coordenador', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_COORDENADOR');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_ADMINISTRADOR', 'Permissão do perfil Administrador', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_ADMINISTRADOR');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_SUPERVISOR', 'Permissão do perfil Supervisor', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_SUPERVISOR');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_TECNICO', 'Permissão do perfil Técnico', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_TECNICO');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_TECNICO_CASA_CIDADAO', 'Permissão do perfil Técnico Casa Cidadão', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_TECNICO_CASA_CIDADAO');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_TECNICO_CAMINHAO', 'Permissão do perfil Técnico Caminhão', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_TECNICO_CAMINHAO');

-- Grupos
INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Analista', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Analista');

INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Secretário', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Secretário');

INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Coordenador', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Coordenador');

INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Administrador', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Administrador');

INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Supervisor', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Supervisor');

INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Técnico', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Técnico');

INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
SELECT nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), 'ROLE_GESTÃO_SPS', 'Permissão do perfil Gestão SPS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE nome = 'ROLE_GESTÃO_SPS');

INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
SELECT nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Gestão SPS', TRUE
WHERE NOT EXISTS (SELECT 1 FROM grupo WHERE nome = 'Gestão SPS');

-- Associação grupo_permissao (cada grupo com sua permissão correspondente)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM grupo g
CROSS JOIN permissao p
WHERE (g.nome = 'Analista'      AND p.nome = 'ROLE_ANALISTA')
   OR (g.nome = 'Secretário'    AND p.nome = 'ROLE_SECRETARIO')
   OR (g.nome = 'Coordenador'   AND p.nome = 'ROLE_COORDENADOR')
   OR (g.nome = 'Administrador' AND p.nome = 'ROLE_ADMINISTRADOR')
   OR (g.nome = 'Supervisor'    AND p.nome = 'ROLE_SUPERVISOR')
   OR (g.nome = 'Técnico'       AND p.nome = 'ROLE_TECNICO')
   OR (g.nome = 'Técnico Casa Cidadão' AND p.nome = 'ROLE_TECNICO_CASA_CIDADAO')
   OR (g.nome = 'Técnico Caminhão' AND p.nome = 'ROLE_TECNICO_CAMINHAO')
   OR (g.nome = 'Gestão SPS'    AND p.nome = 'ROLE_GESTÃO_SPS')
AND NOT EXISTS (
  SELECT 1 FROM grupo_permissao gp WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);
