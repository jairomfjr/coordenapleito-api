CREATE SEQUENCE IF NOT EXISTS acao_id_seq START 1;

CREATE TABLE IF NOT EXISTS acao (
    id BIGINT NOT NULL DEFAULT nextval('acao_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    descricao VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_acao PRIMARY KEY (id),
    CONSTRAINT uq_acao_codigo UNIQUE (codigo),
    CONSTRAINT uq_acao_descricao UNIQUE (descricao)
);
