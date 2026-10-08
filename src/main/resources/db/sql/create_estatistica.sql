CREATE SEQUENCE IF NOT EXISTS estatistica_id_seq START 1;

CREATE TABLE IF NOT EXISTS estatistica (
    id BIGINT NOT NULL DEFAULT nextval('estatistica_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    periodo_acao_id BIGINT NOT NULL,
    equipamento_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL DEFAULT 0,
    observacao VARCHAR(1000),
    CONSTRAINT pk_estatistica PRIMARY KEY (id),
    CONSTRAINT uq_estatistica_codigo UNIQUE (codigo),
    CONSTRAINT uq_estatistica_periodo_acao_equipamento UNIQUE (periodo_acao_id, equipamento_id),
    CONSTRAINT fk_estatistica_periodo_acao FOREIGN KEY (periodo_acao_id) REFERENCES periodo_acao (id),
    CONSTRAINT fk_estatistica_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamento (id),
    CONSTRAINT chk_estatistica_quantidade CHECK (quantidade >= 0)
);
