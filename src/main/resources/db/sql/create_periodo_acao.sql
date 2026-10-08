CREATE SEQUENCE IF NOT EXISTS periodo_acao_id_seq START 1;

CREATE TABLE IF NOT EXISTS periodo_acao (
    id BIGINT NOT NULL DEFAULT nextval('periodo_acao_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    periodo_id BIGINT NOT NULL,
    acao_id BIGINT NOT NULL,
    CONSTRAINT pk_periodo_acao PRIMARY KEY (id),
    CONSTRAINT uq_periodo_acao_codigo UNIQUE (codigo),
    CONSTRAINT uq_periodo_acao_periodo_acao UNIQUE (periodo_id, acao_id),
    CONSTRAINT fk_periodo_acao_periodo FOREIGN KEY (periodo_id) REFERENCES periodo (id),
    CONSTRAINT fk_periodo_acao_acao FOREIGN KEY (acao_id) REFERENCES acao (id)
);
