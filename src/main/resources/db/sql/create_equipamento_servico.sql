CREATE SEQUENCE IF NOT EXISTS equipamento_servico_id_seq START 1;

CREATE TABLE IF NOT EXISTS equipamento_servico (
    id BIGINT NOT NULL DEFAULT nextval('equipamento_servico_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    equipamento_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    categoria_id BIGINT,
    tipo_servico_id BIGINT,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    CONSTRAINT pk_equipamento_servico PRIMARY KEY (id),
    CONSTRAINT uq_equipamento_servico_codigo UNIQUE (codigo),
    CONSTRAINT uq_equipamento_servico_vinculo UNIQUE (equipamento_id, servico_id, mes, ano),
    CONSTRAINT fk_equipamento_servico_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamento(id),
    CONSTRAINT fk_equipamento_servico_servico FOREIGN KEY (servico_id) REFERENCES servico(id),
    CONSTRAINT fk_equipamento_servico_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT fk_equipamento_servico_tipo_servico FOREIGN KEY (tipo_servico_id) REFERENCES tipo_servico(id)
);
