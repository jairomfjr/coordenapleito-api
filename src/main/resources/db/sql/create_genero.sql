CREATE SEQUENCE IF NOT EXISTS genero_id_seq START 1;

CREATE TABLE IF NOT EXISTS genero (
    id BIGINT NOT NULL DEFAULT nextval('genero_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_genero PRIMARY KEY (id),
    CONSTRAINT uq_genero_codigo UNIQUE (codigo),
    CONSTRAINT uq_genero_nome UNIQUE (nome)
);
