CREATE SEQUENCE IF NOT EXISTS equipamento_servico_cidadao_id_seq START 1;

CREATE TABLE IF NOT EXISTS equipamento_servico_cidadao (
    id BIGINT NOT NULL DEFAULT nextval('equipamento_servico_cidadao_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    equipamento_servico_id BIGINT NOT NULL,
    cidadao_id BIGINT NOT NULL,
    CONSTRAINT pk_equipamento_servico_cidadao PRIMARY KEY (id),
    CONSTRAINT uq_equipamento_servico_cidadao_codigo UNIQUE (codigo),
    CONSTRAINT uq_equipamento_servico_cidadao_vinculo UNIQUE (equipamento_servico_id, cidadao_id),
    CONSTRAINT fk_equipamento_servico_cidadao_es FOREIGN KEY (equipamento_servico_id) REFERENCES equipamento_servico(id),
    CONSTRAINT fk_equipamento_servico_cidadao_cidadao FOREIGN KEY (cidadao_id) REFERENCES cidadao(id)
);
