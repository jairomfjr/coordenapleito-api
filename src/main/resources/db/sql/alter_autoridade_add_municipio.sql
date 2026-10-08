-- Município vinculado (opcional); a API aceita apenas municípios do Ceará (UF = CE).

ALTER TABLE autoridade ADD COLUMN IF NOT EXISTS municipio_id BIGINT NULL;

ALTER TABLE autoridade DROP CONSTRAINT IF EXISTS autoridade_municipio_fk;

ALTER TABLE autoridade
    ADD CONSTRAINT autoridade_municipio_fk FOREIGN KEY (municipio_id) REFERENCES municipio (id);

CREATE INDEX IF NOT EXISTS idx_autoridade_municipio ON autoridade (municipio_id);
