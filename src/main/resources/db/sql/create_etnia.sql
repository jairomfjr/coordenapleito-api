CREATE SEQUENCE IF NOT EXISTS etnia_id_seq START 1;

CREATE TABLE IF NOT EXISTS etnia (
    id BIGINT NOT NULL DEFAULT nextval('etnia_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_etnia PRIMARY KEY (id),
    CONSTRAINT uq_etnia_codigo UNIQUE (codigo),
    CONSTRAINT uq_etnia_nome UNIQUE (nome)
);
