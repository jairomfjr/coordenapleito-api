-- CMIC: folhas de pagamento e municípios por folha com quantidade de beneficiários

-- Sequências
CREATE SEQUENCE IF NOT EXISTS cmic_folha_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS cmic_folha_municipio_id_seq START 1;

-- Tabela folha de pagamento (mês/ano)
CREATE TABLE IF NOT EXISTS cmic_folha (
    id BIGINT NOT NULL DEFAULT nextval('cmic_folha_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    valor_folha NUMERIC(19, 2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_cmic_folha PRIMARY KEY (id),
    CONSTRAINT uq_cmic_folha_codigo UNIQUE (codigo),
    CONSTRAINT chk_cmic_folha_mes CHECK (mes >= 1 AND mes <= 12),
    CONSTRAINT chk_cmic_folha_ano CHECK (ano >= 1900 AND ano <= 2100)
);

-- Tabela município por folha (beneficiários por município)
CREATE TABLE IF NOT EXISTS cmic_folha_municipio (
    id BIGINT NOT NULL DEFAULT nextval('cmic_folha_municipio_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    cmic_folha_id BIGINT NOT NULL,
    municipio_id BIGINT NOT NULL,
    quantidade_beneficiarios INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_cmic_folha_municipio PRIMARY KEY (id),
    CONSTRAINT uq_cmic_folha_municipio_folha_municipio UNIQUE (cmic_folha_id, municipio_id),
    CONSTRAINT fk_cmic_folha_municipio_folha FOREIGN KEY (cmic_folha_id) REFERENCES cmic_folha(id) ON DELETE CASCADE,
    CONSTRAINT fk_cmic_folha_municipio_municipio FOREIGN KEY (municipio_id) REFERENCES municipio(id),
    CONSTRAINT chk_cmic_folha_municipio_qtd CHECK (quantidade_beneficiarios >= 0)
);

CREATE INDEX IF NOT EXISTS idx_cmic_folha_municipio_folha ON cmic_folha_municipio(cmic_folha_id);
CREATE INDEX IF NOT EXISTS idx_cmic_folha_municipio_municipio ON cmic_folha_municipio(municipio_id);
