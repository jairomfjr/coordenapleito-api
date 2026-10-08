CREATE SEQUENCE IF NOT EXISTS partido_politico_id_seq START 1;

CREATE TABLE IF NOT EXISTS partido_politico (
    id BIGINT NOT NULL DEFAULT nextval('partido_politico_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    descricao VARCHAR(255) NOT NULL,
    sigla VARCHAR(32) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_partido_politico PRIMARY KEY (id),
    CONSTRAINT uq_partido_politico_codigo UNIQUE (codigo),
    CONSTRAINT uq_partido_politico_sigla UNIQUE (sigla)
);
