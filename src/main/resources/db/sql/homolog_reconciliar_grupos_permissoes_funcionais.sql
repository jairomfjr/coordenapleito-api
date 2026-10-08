-- =============================================================================
-- Homolog: reconciliar grupo_permissao (legado ROLE_* → RBAC funcional por chave)
-- =============================================================================
--
-- Quando usar:
--   - Banco já com V31–V32 (colunas chave + catálogo funcional).
--   - V33 rodou parcialmente, com mapeamento antigo/errado, ou ainda só ROLE_*.
--
-- O que faz (por grupo legado listado abaixo):
--   1) Remove vínculos com permissões ROLE_* (inativas).
--   2) Remove vínculos funcionais (chave preenchida) desse grupo.
--   3) Reinsere o conjunto conforme matriz legada (PermissoesGrupoLegadoHelper / V33).
--
-- NÃO altera grupos customizados fora da lista.
-- Idempotente: pode rodar mais de uma vez.
--
-- Antes: backup ou snapshot; conferir com consultas da seção "Auditoria" no final.
-- Depois: usuários devem fazer novo login.
--
-- Referência: docs/legacy-group-permissions-matrix.md
--
-- Sem acesso a /usuarios ou /grupos? Rode antes (só Analista + Administrador full):
--   homolog_bootstrap_analista_acesso_total.sql
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 1) Remover ROLE_* de todos os grupos (vínculo legado inútil no JWT novo)
-- ---------------------------------------------------------------------------
DELETE FROM grupo_permissao gp
USING permissao p
WHERE gp.permissao_id = p.id
  AND p.nome LIKE 'ROLE\_%' ESCAPE '\';

-- ---------------------------------------------------------------------------
-- 2) Administrador — catálogo funcional completo
-- ---------------------------------------------------------------------------
WITH g AS (
    SELECT id FROM grupo WHERE nome = 'Administrador' AND ativo = TRUE
),
_ AS (
    DELETE FROM grupo_permissao gp
    USING g, permissao p
    WHERE gp.grupo_id = g.id
      AND gp.permissao_id = p.id
      AND p.chave IS NOT NULL
)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM g
JOIN permissao p ON p.ativo = TRUE AND p.chave IS NOT NULL
WHERE NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp
    WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);

-- ---------------------------------------------------------------------------
-- 3) Analista — todas as chaves ativas
-- ---------------------------------------------------------------------------
WITH g AS (
    SELECT id FROM grupo WHERE nome = 'Analista' AND ativo = TRUE
),
_ AS (
    DELETE FROM grupo_permissao gp
    USING g, permissao p
    WHERE gp.grupo_id = g.id
      AND gp.permissao_id = p.id
      AND p.chave IS NOT NULL
)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM g
JOIN permissao p ON p.ativo = TRUE AND p.chave IS NOT NULL
WHERE NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp
    WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);

-- ---------------------------------------------------------------------------
-- 4) Secretário / Secretario — tudo exceto Vapt (cadastro Analista)
-- ---------------------------------------------------------------------------
WITH g AS (
    SELECT id FROM grupo
    WHERE nome IN ('Secretário', 'Secretario') AND ativo = TRUE
),
_ AS (
    DELETE FROM grupo_permissao gp
    USING g, permissao p
    WHERE gp.grupo_id = g.id
      AND gp.permissao_id = p.id
      AND p.chave IS NOT NULL
)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM g
JOIN permissao p ON p.ativo = TRUE AND p.chave IS NOT NULL
WHERE p.recurso NOT IN ('servico-vapt-vupt', 'orgao-vapt-vupt')
  AND NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp
    WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);

-- ---------------------------------------------------------------------------
-- 5) Gestão SPS — subconjunto operacional + escopo listar-todos
-- ---------------------------------------------------------------------------
WITH g AS (
    SELECT id FROM grupo WHERE nome = 'Gestão SPS' AND ativo = TRUE
),
recursos AS (
    SELECT unnest(ARRAY[
        'tipo-equipamento', 'categoria', 'tipo-servico', 'servico', 'coordenacao',
        'organograma', 'dashboard', 'equipamento', 'equipamento-servico', 'cidadao',
        'coordenacao-basica', 'inclusao-social', 'periodo', 'periodo-acao', 'acolhimento',
        'estatistica'
    ]) AS recurso
),
_ AS (
    DELETE FROM grupo_permissao gp
    USING g, permissao p
    WHERE gp.grupo_id = g.id
      AND gp.permissao_id = p.id
      AND p.chave IS NOT NULL
)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM g
JOIN permissao p ON p.ativo = TRUE AND p.chave IS NOT NULL
WHERE (
    p.recurso IN (SELECT recurso FROM recursos)
    OR p.chave IN ('equipamento.listar-todos', 'estatistica.listar-todos')
)
AND NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp
    WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);

-- ---------------------------------------------------------------------------
-- 6) Coordenador — coordenações/projetos + escopo listar-coordenacao
-- ---------------------------------------------------------------------------
WITH g AS (
    SELECT id FROM grupo WHERE nome = 'Coordenador' AND ativo = TRUE
),
recursos AS (
    SELECT unnest(ARRAY[
        'coordenacao-basica', 'inclusao-social', 'cidadao', 'equipamento', 'equipamento-servico',
        'estatistica', 'casa-cidadao', 'casa-cidadao-atendimento', 'caminhao-cidadao',
        'caminhao-cidadao-atendimento'
    ]) AS recurso
),
_ AS (
    DELETE FROM grupo_permissao gp
    USING g, permissao p
    WHERE gp.grupo_id = g.id
      AND gp.permissao_id = p.id
      AND p.chave IS NOT NULL
)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM g
JOIN permissao p ON p.ativo = TRUE AND p.chave IS NOT NULL
WHERE (
    p.recurso IN (SELECT recurso FROM recursos)
    OR p.chave IN ('equipamento.listar-coordenacao', 'estatistica.listar-coordenacao')
)
AND NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp
    WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);

-- ---------------------------------------------------------------------------
-- 7) Supervisor e Técnico — operação vinculada + listar-vinculados
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    nomes TEXT[] := ARRAY['Supervisor', 'Técnico'];
    nome_grupo TEXT;
    gid BIGINT;
BEGIN
    FOREACH nome_grupo IN ARRAY nomes LOOP
        SELECT id INTO gid FROM grupo WHERE grupo.nome = nome_grupo AND ativo = TRUE;
        IF gid IS NULL THEN
            CONTINUE;
        END IF;

        DELETE FROM grupo_permissao gp
        USING permissao p
        WHERE gp.grupo_id = gid
          AND gp.permissao_id = p.id
          AND p.chave IS NOT NULL;

        INSERT INTO grupo_permissao (grupo_id, permissao_id)
        SELECT gid, p.id
        FROM permissao p
        WHERE p.ativo = TRUE
          AND p.chave IS NOT NULL
          AND (
              p.recurso IN ('equipamento', 'equipamento-servico', 'cidadao', 'estatistica')
              OR p.chave IN ('equipamento.listar-vinculados', 'estatistica.listar-vinculados')
          )
          AND NOT EXISTS (
              SELECT 1 FROM grupo_permissao gp
              WHERE gp.grupo_id = gid AND gp.permissao_id = p.id
          );
    END LOOP;
END $$;

-- ---------------------------------------------------------------------------
-- 8) Técnico Casa Cidadão
-- ---------------------------------------------------------------------------
WITH g AS (
    SELECT id FROM grupo WHERE nome = 'Técnico Casa Cidadão' AND ativo = TRUE
),
recursos AS (
    SELECT unnest(ARRAY[
        'casa-cidadao', 'casa-cidadao-atendimento', 'cidadao', 'servico-vapt-vupt'
    ]) AS recurso
),
_ AS (
    DELETE FROM grupo_permissao gp
    USING g, permissao p
    WHERE gp.grupo_id = g.id
      AND gp.permissao_id = p.id
      AND p.chave IS NOT NULL
)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM g
JOIN permissao p ON p.ativo = TRUE AND p.chave IS NOT NULL
WHERE p.recurso IN (SELECT recurso FROM recursos)
  AND NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp
    WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);

-- ---------------------------------------------------------------------------
-- 9) Técnico Caminhão
-- ---------------------------------------------------------------------------
WITH g AS (
    SELECT id FROM grupo WHERE nome = 'Técnico Caminhão' AND ativo = TRUE
),
recursos AS (
    SELECT unnest(ARRAY[
        'caminhao-cidadao', 'caminhao-cidadao-atendimento', 'cidadao', 'servico-caminhao'
    ]) AS recurso
),
_ AS (
    DELETE FROM grupo_permissao gp
    USING g, permissao p
    WHERE gp.grupo_id = g.id
      AND gp.permissao_id = p.id
      AND p.chave IS NOT NULL
)
INSERT INTO grupo_permissao (grupo_id, permissao_id)
SELECT g.id, p.id
FROM g
JOIN permissao p ON p.ativo = TRUE AND p.chave IS NOT NULL
WHERE p.recurso IN (SELECT recurso FROM recursos)
  AND NOT EXISTS (
    SELECT 1 FROM grupo_permissao gp
    WHERE gp.grupo_id = g.id AND gp.permissao_id = p.id
);

COMMIT;

-- =============================================================================
-- Auditoria (rodar após o script)
-- =============================================================================
-- SELECT g.nome AS grupo, p.nome, p.chave, p.ativo
-- FROM grupo g
-- JOIN grupo_permissao gp ON gp.grupo_id = g.id
-- JOIN permissao p ON p.id = gp.permissao_id
-- WHERE g.ativo
-- ORDER BY g.nome, p.chave NULLS LAST, p.nome;
--
-- SELECT g.nome,
--        COUNT(*) FILTER (WHERE p.chave IS NOT NULL) AS qtd_funcionais,
--        COUNT(*) FILTER (WHERE p.nome LIKE 'ROLE_%') AS qtd_role_legado
-- FROM grupo g
-- JOIN grupo_permissao gp ON gp.grupo_id = g.id
-- JOIN permissao p ON p.id = gp.permissao_id
-- WHERE g.nome IN (
--   'Administrador', 'Analista', 'Secretário', 'Secretario', 'Gestão SPS',
--   'Coordenador', 'Supervisor', 'Técnico', 'Técnico Casa Cidadão', 'Técnico Caminhão'
-- )
-- GROUP BY g.nome
-- ORDER BY g.nome;
--
-- Esperado: qtd_role_legado = 0; Administrador/Analista com centenas de funcionais;
-- Gestão SPS ~dezenas; Supervisor/Técnico ~dezenas; técnicos projetos ~dezenas.
