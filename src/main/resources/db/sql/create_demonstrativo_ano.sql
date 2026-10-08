-- SECOFI: demonstrativo por ano e municípios por demonstrativo com quantidade de beneficiários

-- Sequências
CREATE SEQUENCE IF NOT EXISTS demonstrativo_ano_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS demonstrativo_ano_municipio_id_seq START 1;

-- Tabela demonstrativo (ano)
CREATE TABLE IF NOT EXISTS demonstrativo_ano (
    id BIGINT NOT NULL DEFAULT nextval('demonstrativo_ano_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    ano INTEGER NOT NULL,
    CONSTRAINT pk_demonstrativo_ano PRIMARY KEY (id),
    CONSTRAINT uq_demonstrativo_ano_codigo UNIQUE (codigo),
    CONSTRAINT chk_demonstrativo_ano_ano CHECK (ano >= 1900 AND ano <= 2100)
);

-- Tabela município por demonstrativo (beneficiários por município)
CREATE TABLE IF NOT EXISTS demonstrativo_ano_municipio (
    id BIGINT NOT NULL DEFAULT nextval('demonstrativo_ano_municipio_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    demonstrativo_ano_id BIGINT NOT NULL,
    municipio_id BIGINT NOT NULL,
    quantidade_beneficiarios INTEGER NOT NULL DEFAULT 0,
    total_be_item_15 DOUBLE PRECISION,
    total_paif_item_15 DOUBLE PRECISION,
    total_paefi_item_15 DOUBLE PRECISION,
    CONSTRAINT pk_demonstrativo_ano_municipio PRIMARY KEY (id),
    CONSTRAINT uq_demonstrativo_ano_municipio_demo_municipio UNIQUE (demonstrativo_ano_id, municipio_id),
    CONSTRAINT fk_demonstrativo_ano_municipio_demo FOREIGN KEY (demonstrativo_ano_id) REFERENCES demonstrativo_ano(id) ON DELETE CASCADE,
    CONSTRAINT fk_demonstrativo_ano_municipio_municipio FOREIGN KEY (municipio_id) REFERENCES municipio(id),
    CONSTRAINT chk_demonstrativo_ano_municipio_qtd CHECK (quantidade_beneficiarios >= 0)
);

CREATE INDEX IF NOT EXISTS idx_demonstrativo_ano_municipio_demo ON demonstrativo_ano_municipio(demonstrativo_ano_id);
CREATE INDEX IF NOT EXISTS idx_demonstrativo_ano_municipio_municipio ON demonstrativo_ano_municipio(municipio_id);
