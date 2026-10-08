-- Executar após create_etnia.sql. Adiciona raça/cor (enum string) e vínculo opcional com etnia.

ALTER TABLE cidadao ADD COLUMN IF NOT EXISTS raca_cor VARCHAR(32);
ALTER TABLE cidadao ADD COLUMN IF NOT EXISTS etnia_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cidadao_etnia'
    ) THEN
        ALTER TABLE cidadao
            ADD CONSTRAINT fk_cidadao_etnia
            FOREIGN KEY (etnia_id) REFERENCES etnia (id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_cidadao_etnia ON cidadao (etnia_id);
