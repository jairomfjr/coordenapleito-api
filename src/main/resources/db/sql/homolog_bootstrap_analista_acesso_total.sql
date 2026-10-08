-- =============================================================================
-- Homolog — desbloqueio: Analista e Administrador com TODAS as permissões funcionais
-- =============================================================================
--
-- Use quando não conseguir abrir /usuarios ou /grupos (JWT sem chaves funcionais).
-- Pré-requisito: Flyway V31 + V32 (coluna chave e catálogo em `permissao`).
--
-- Depois: logout + login novamente.
-- =============================================================================

BEGIN;

DO $$
DECLARE
    qtd_catalogo BIGINT;
BEGIN
    SELECT COUNT(*) INTO qtd_catalogo
    FROM permissao
    WHERE ativo = TRUE AND chave IS NOT NULL AND TRIM(chave) <> '';

    IF qtd_catalogo < 50 THEN
        RAISE EXCEPTION
            'Catálogo funcional insuficiente (% chaves). Execute as migrations V31 e V32 antes deste script.',
            qtd_catalogo;
    END IF;
END $$;

-- Remove vínculos legados ROLE_* (não entram mais no JWT)
DELETE FROM grupo_permissao gp
USING permissao p
WHERE gp.permissao_id = p.id
  AND p.nome LIKE 'ROLE\_%' ESCAPE '\';

DO $$
DECLARE
    nome_grupo TEXT;
    gid BIGINT;
BEGIN
    FOREACH nome_grupo IN ARRAY ARRAY['Analista', 'Administrador'] LOOP
        SELECT id INTO gid FROM grupo WHERE grupo.nome = nome_grupo AND ativo = TRUE;
        IF gid IS NULL THEN
            RAISE NOTICE 'Grupo "%" não encontrado; ignorado.', nome_grupo;
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
          AND TRIM(p.chave) <> ''
          AND NOT EXISTS (
              SELECT 1 FROM grupo_permissao gp
              WHERE gp.grupo_id = gid AND gp.permissao_id = p.id
          );

        RAISE NOTICE 'Grupo "%": permissões funcionais reaplicadas.', nome_grupo;
    END LOOP;
END $$;

COMMIT;

-- Verificação:
-- SELECT g.nome, COUNT(*) FILTER (WHERE p.chave IS NOT NULL) AS funcionais
-- FROM grupo g
-- JOIN grupo_permissao gp ON gp.grupo_id = g.id
-- JOIN permissao p ON p.id = gp.permissao_id
-- WHERE g.nome IN ('Analista', 'Administrador')
-- GROUP BY g.nome;
