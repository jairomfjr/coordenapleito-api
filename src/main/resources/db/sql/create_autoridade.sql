CREATE SEQUENCE IF NOT EXISTS autoridade_id_seq START 1;

CREATE TABLE IF NOT EXISTS autoridade (
    id BIGINT NOT NULL DEFAULT nextval('autoridade_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    nome VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    cargo_id BIGINT NOT NULL,
    CONSTRAINT pk_autoridade PRIMARY KEY (id),
    CONSTRAINT uq_autoridade_codigo UNIQUE (codigo),
    CONSTRAINT fk_autoridade_cargo FOREIGN KEY (cargo_id) REFERENCES cargo (id)
);

CREATE INDEX IF NOT EXISTS idx_autoridade_cargo_id ON autoridade (cargo_id);
