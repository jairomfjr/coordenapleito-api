CREATE SEQUENCE IF NOT EXISTS orientacao_sexual_id_seq START 1;

CREATE TABLE IF NOT EXISTS orientacao_sexual (
    id BIGINT NOT NULL DEFAULT nextval('orientacao_sexual_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_orientacao_sexual PRIMARY KEY (id),
    CONSTRAINT uq_orientacao_sexual_codigo UNIQUE (codigo),
    CONSTRAINT uq_orientacao_sexual_nome UNIQUE (nome)
);
