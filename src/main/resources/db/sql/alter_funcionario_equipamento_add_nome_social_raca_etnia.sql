-- Nome social, raça/cor (enum string) e etnia opcional em funcionário de equipamento.

ALTER TABLE funcionario_equipamento ADD COLUMN IF NOT EXISTS nome_social VARCHAR(255);
ALTER TABLE funcionario_equipamento ADD COLUMN IF NOT EXISTS raca_cor VARCHAR(32);
ALTER TABLE funcionario_equipamento ADD COLUMN IF NOT EXISTS etnia_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_funcionario_equipamento_etnia'
    ) THEN
        ALTER TABLE funcionario_equipamento
            ADD CONSTRAINT fk_funcionario_equipamento_etnia
            FOREIGN KEY (etnia_id) REFERENCES etnia (id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_funcionario_equipamento_etnia ON funcionario_equipamento (etnia_id);
