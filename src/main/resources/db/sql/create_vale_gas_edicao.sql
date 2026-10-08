-- Vale Gás: edições (mês/ano) e municípios por edição com quantidade de beneficiários

-- Sequências
CREATE SEQUENCE IF NOT EXISTS vale_gas_edicao_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS vale_gas_edicao_municipio_id_seq START 1;

-- Tabela edição (mês/ano)
CREATE TABLE IF NOT EXISTS vale_gas_edicao (
    id BIGINT NOT NULL DEFAULT nextval('vale_gas_edicao_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    valor_voucher NUMERIC(19, 2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_vale_gas_edicao PRIMARY KEY (id),
    CONSTRAINT uq_vale_gas_edicao_codigo UNIQUE (codigo),
    CONSTRAINT chk_vale_gas_edicao_mes CHECK (mes >= 1 AND mes <= 12),
    CONSTRAINT chk_vale_gas_edicao_ano CHECK (ano >= 1900 AND ano <= 2100)
);

-- Tabela município por edição (beneficiários por município)
CREATE TABLE IF NOT EXISTS vale_gas_edicao_municipio (
    id BIGINT NOT NULL DEFAULT nextval('vale_gas_edicao_municipio_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    vale_gas_edicao_id BIGINT NOT NULL,
    municipio_id BIGINT NOT NULL,
    quantidade_beneficiarios INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_vale_gas_edicao_municipio PRIMARY KEY (id),
    CONSTRAINT uq_vale_gas_edicao_municipio_edicao_municipio UNIQUE (vale_gas_edicao_id, municipio_id),
    CONSTRAINT fk_vale_gas_edicao_municipio_edicao FOREIGN KEY (vale_gas_edicao_id) REFERENCES vale_gas_edicao(id) ON DELETE CASCADE,
    CONSTRAINT fk_vale_gas_edicao_municipio_municipio FOREIGN KEY (municipio_id) REFERENCES municipio(id),
    CONSTRAINT chk_vale_gas_edicao_municipio_qtd CHECK (quantidade_beneficiarios >= 0)
);

CREATE INDEX IF NOT EXISTS idx_vale_gas_edicao_municipio_edicao ON vale_gas_edicao_municipio(vale_gas_edicao_id);
CREATE INDEX IF NOT EXISTS idx_vale_gas_edicao_municipio_municipio ON vale_gas_edicao_municipio(municipio_id);
