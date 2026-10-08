-- Opcional: preenche descrição de serviços importados com o nome legível de outro registro
-- do mesmo id_origem (quando existir). Execute uma vez no PostgreSQL se o dashboard ainda
-- exibir "Serviço importado do sistema antigo (id_origem=…)".

UPDATE servico_vapt_vupt s_imp
SET descricao = nome.nome,
    data_ultima_atualizacao = NOW()
FROM (
    SELECT
        COALESCE(
            s.id_origem,
            NULLIF(substring(s.descricao FROM '(?i)id_origem\s*=\s*(\d+)'), '')::bigint
        ) AS id_origem_efetivo,
        MAX(s.descricao) FILTER (
            WHERE s.descricao NOT ILIKE '%importado do sistema antigo%'
        ) AS nome
    FROM servico_vapt_vupt s
    GROUP BY 1
    HAVING MAX(s.descricao) FILTER (
        WHERE s.descricao NOT ILIKE '%importado do sistema antigo%'
    ) IS NOT NULL
) nome
WHERE s_imp.descricao ILIKE '%importado do sistema antigo%'
  AND COALESCE(
        s_imp.id_origem,
        NULLIF(substring(s_imp.descricao FROM '(?i)id_origem\s*=\s*(\d+)'), '')::bigint
      ) = nome.id_origem_efetivo;
