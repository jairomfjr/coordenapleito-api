-- Solicitante: autoridade vinculada ao mesmo município da demanda.
-- Em bases que já tinham demanda_municipio sem esta coluna: adiciona como NULL até preencher e aplicar NOT NULL se desejar.

ALTER TABLE demanda_municipio ADD COLUMN IF NOT EXISTS solicitante_id BIGINT NULL;

ALTER TABLE demanda_municipio DROP CONSTRAINT IF EXISTS demanda_municipio_solicitante_fk;

ALTER TABLE demanda_municipio
    ADD CONSTRAINT demanda_municipio_solicitante_fk FOREIGN KEY (solicitante_id) REFERENCES autoridade (id);

CREATE INDEX IF NOT EXISTS idx_demanda_municipio_solicitante ON demanda_municipio (solicitante_id);
