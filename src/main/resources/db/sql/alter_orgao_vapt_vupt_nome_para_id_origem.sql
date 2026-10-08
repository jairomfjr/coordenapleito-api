-- Migração: substitui coluna `nome` por `id_orgao_origem` (Banco já criado com o script antigo).
-- Se houver linhas com apenas `nome` e sem id de origem, ajuste manualmente após a migração.

ALTER TABLE orgao_vapt_vupt DROP CONSTRAINT IF EXISTS uq_orgao_vapt_vupt_nome;
DROP INDEX IF EXISTS idx_orgao_vapt_vupt_nome;
ALTER TABLE orgao_vapt_vupt DROP COLUMN IF EXISTS nome;

ALTER TABLE orgao_vapt_vupt ADD COLUMN IF NOT EXISTS id_orgao_origem BIGINT;
UPDATE orgao_vapt_vupt SET id_orgao_origem = id WHERE id_orgao_origem IS NULL;
ALTER TABLE orgao_vapt_vupt ALTER COLUMN id_orgao_origem SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_orgao_vapt_vupt_id_origem'
    ) THEN
        ALTER TABLE orgao_vapt_vupt ADD CONSTRAINT uq_orgao_vapt_vupt_id_origem UNIQUE (id_orgao_origem);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_orgao_vapt_vupt_id_origem ON orgao_vapt_vupt(id_orgao_origem);
