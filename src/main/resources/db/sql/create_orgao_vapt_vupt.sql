-- Cadastro de órgãos VAPT VUPT (dados básicos, perfil Analista)

CREATE SEQUENCE IF NOT EXISTS orgao_vapt_vupt_id_seq START 1;

CREATE TABLE IF NOT EXISTS orgao_vapt_vupt (
    id BIGINT NOT NULL DEFAULT nextval('orgao_vapt_vupt_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    id_orgao_origem BIGINT NOT NULL,
    descricao VARCHAR(2000),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_orgao_vapt_vupt PRIMARY KEY (id),
    CONSTRAINT uq_orgao_vapt_vupt_codigo UNIQUE (codigo),
    CONSTRAINT uq_orgao_vapt_vupt_id_origem UNIQUE (id_orgao_origem)
);

CREATE INDEX IF NOT EXISTS idx_orgao_vapt_vupt_id_origem ON orgao_vapt_vupt(id_orgao_origem);
