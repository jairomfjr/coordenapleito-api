-- Migra colunas id_orgao / id_servico (valores de origem ou eventualmente PK) para FKs orgao_vapt_vupt_id e servico_vapt_vupt_id.
-- PostgreSQL. Executar uma vez em bases que ainda possuem id_orgao e id_servico.
-- Revise linhas órfãs antes de aplicar em produção (UPDATE pode deixar NULL se não houver cadastro).

ALTER TABLE vapt_vupt_atendimento_cidadao
    ADD COLUMN IF NOT EXISTS orgao_vapt_vupt_id BIGINT,
    ADD COLUMN IF NOT EXISTS servico_vapt_vupt_id BIGINT;

UPDATE vapt_vupt_atendimento_cidadao c
SET orgao_vapt_vupt_id = o.id
FROM orgao_vapt_vupt o
WHERE c.orgao_vapt_vupt_id IS NULL
  AND c.id_orgao IS NOT NULL
  AND (o.id_orgao_origem = c.id_orgao OR o.id = c.id_orgao);

UPDATE vapt_vupt_atendimento_cidadao c
SET servico_vapt_vupt_id = s.id
FROM servico_vapt_vupt s
WHERE c.servico_vapt_vupt_id IS NULL
  AND c.id_servico IS NOT NULL
  AND (s.id_origem = c.id_servico OR s.id = c.id_servico);

-- Descomente para descartar registros sem cadastro correspondente (ajuste conforme política de dados):
-- DELETE FROM vapt_vupt_atendimento_cidadao WHERE orgao_vapt_vupt_id IS NULL OR servico_vapt_vupt_id IS NULL;

ALTER TABLE vapt_vupt_atendimento_cidadao
    ALTER COLUMN orgao_vapt_vupt_id SET NOT NULL,
    ALTER COLUMN servico_vapt_vupt_id SET NOT NULL;

ALTER TABLE vapt_vupt_atendimento_cidadao DROP CONSTRAINT IF EXISTS uq_vapt_vupt_atendimento_cidadao_triplo;

DROP INDEX IF EXISTS idx_vvacc_id_orgao;
DROP INDEX IF EXISTS idx_vvacc_id_servico;

ALTER TABLE vapt_vupt_atendimento_cidadao DROP COLUMN IF EXISTS id_orgao;
ALTER TABLE vapt_vupt_atendimento_cidadao DROP COLUMN IF EXISTS id_servico;

ALTER TABLE vapt_vupt_atendimento_cidadao
    ADD CONSTRAINT uq_vapt_vupt_atendimento_cidadao_triplo UNIQUE (vapt_vupt_atendimento_id, orgao_vapt_vupt_id, servico_vapt_vupt_id);

ALTER TABLE vapt_vupt_atendimento_cidadao
    ADD CONSTRAINT fk_vvacc_orgao_vapt_vupt FOREIGN KEY (orgao_vapt_vupt_id) REFERENCES orgao_vapt_vupt(id),
    ADD CONSTRAINT fk_vvacc_servico_vapt_vupt FOREIGN KEY (servico_vapt_vupt_id) REFERENCES servico_vapt_vupt(id);

CREATE INDEX IF NOT EXISTS idx_vvacc_orgao_vapt_vupt ON vapt_vupt_atendimento_cidadao(orgao_vapt_vupt_id);
CREATE INDEX IF NOT EXISTS idx_vvacc_servico_vapt_vupt ON vapt_vupt_atendimento_cidadao(servico_vapt_vupt_id);
