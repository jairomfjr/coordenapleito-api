-- Demandas por município (prazo de 1 ano; após expiração, status SOLICITADO vira REPRIMIDO automaticamente na API).

CREATE SEQUENCE IF NOT EXISTS demanda_municipio_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS demanda_municipio (
    id BIGINT NOT NULL DEFAULT nextval('demanda_municipio_id_seq'),
    codigo UUID NOT NULL,
    descricao TEXT NOT NULL,
    municipio_id BIGINT NOT NULL,
    solicitante_id BIGINT NOT NULL,
    data_prevista_atendimento DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    data_expiracao DATE NOT NULL,
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    CONSTRAINT demanda_municipio_pkey PRIMARY KEY (id),
    CONSTRAINT demanda_municipio_codigo_key UNIQUE (codigo),
    CONSTRAINT demanda_municipio_municipio_fk FOREIGN KEY (municipio_id) REFERENCES municipio (id),
    CONSTRAINT demanda_municipio_solicitante_fk FOREIGN KEY (solicitante_id) REFERENCES autoridade (id)
);

CREATE INDEX IF NOT EXISTS idx_demanda_municipio_municipio ON demanda_municipio (municipio_id);
CREATE INDEX IF NOT EXISTS idx_demanda_municipio_solicitante ON demanda_municipio (solicitante_id);
CREATE INDEX IF NOT EXISTS idx_demanda_municipio_status ON demanda_municipio (status);
CREATE INDEX IF NOT EXISTS idx_demanda_municipio_data_expiracao ON demanda_municipio (data_expiracao);
