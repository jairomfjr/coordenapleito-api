CREATE SEQUENCE IF NOT EXISTS cargo_id_seq START 1;

CREATE TABLE IF NOT EXISTS cargo (
    id BIGINT NOT NULL DEFAULT nextval('cargo_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    descricao VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_cargo PRIMARY KEY (id),
    CONSTRAINT uq_cargo_codigo UNIQUE (codigo),
    CONSTRAINT uq_cargo_descricao UNIQUE (descricao)
);
