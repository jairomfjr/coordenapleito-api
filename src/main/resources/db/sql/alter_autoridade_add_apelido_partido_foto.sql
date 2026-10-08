-- Executar após `create_autoridade.sql` e tabela `partido_politico` existirem.

ALTER TABLE autoridade
    ADD COLUMN IF NOT EXISTS apelido VARCHAR(255),
    ADD COLUMN IF NOT EXISTS partido_politico_id BIGINT,
    ADD COLUMN IF NOT EXISTS foto_caminho VARCHAR(500),
    ADD COLUMN IF NOT EXISTS foto_content_type VARCHAR(100);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_autoridade_partido_politico'
    ) THEN
        ALTER TABLE autoridade
            ADD CONSTRAINT fk_autoridade_partido_politico
            FOREIGN KEY (partido_politico_id) REFERENCES partido_politico (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_autoridade_partido_politico_id ON autoridade (partido_politico_id);
