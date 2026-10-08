CREATE SEQUENCE IF NOT EXISTS coordenacao_id_seq START 1;

CREATE TABLE IF NOT EXISTS coordenacao (
    id BIGINT NOT NULL DEFAULT nextval('coordenacao_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    descricao VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_coordenacao PRIMARY KEY (id),
    CONSTRAINT uq_coordenacao_codigo UNIQUE (codigo),
    CONSTRAINT uq_coordenacao_descricao UNIQUE (descricao)
);
