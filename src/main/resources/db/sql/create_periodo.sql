-- Cadastro de período (mês/ano)

CREATE SEQUENCE IF NOT EXISTS periodo_id_seq START 1;

CREATE TABLE IF NOT EXISTS periodo (
    id BIGINT NOT NULL DEFAULT nextval('periodo_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    descricao VARCHAR(20) NOT NULL,
    CONSTRAINT pk_periodo PRIMARY KEY (id),
    CONSTRAINT uq_periodo_codigo UNIQUE (codigo),
    CONSTRAINT uq_periodo_mes_ano UNIQUE (mes, ano),
    CONSTRAINT chk_periodo_mes CHECK (mes >= 1 AND mes <= 12),
    CONSTRAINT chk_periodo_ano CHECK (ano >= 1900 AND ano <= 2100)
);
