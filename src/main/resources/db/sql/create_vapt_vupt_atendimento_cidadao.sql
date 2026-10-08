-- Quantificação de usuários atendidos no VAPT VUPT por atendimento, órgão e serviço (IDs de origem ETL).

CREATE SEQUENCE IF NOT EXISTS vapt_vupt_atendimento_cidadao_id_seq START 1;



CREATE TABLE IF NOT EXISTS vapt_vupt_atendimento_cidadao (

    id BIGINT NOT NULL DEFAULT nextval('vapt_vupt_atendimento_cidadao_id_seq'),

    codigo UUID NOT NULL DEFAULT gen_random_uuid(),

    data_cadastro TIMESTAMPTZ,

    data_atualizacao TIMESTAMPTZ,

    vapt_vupt_atendimento_id BIGINT NOT NULL,

    id_orgao BIGINT NOT NULL,

    id_servico BIGINT NOT NULL,

    quantidade_usuarios_atendidas INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT pk_vapt_vupt_atendimento_cidadao PRIMARY KEY (id),

    CONSTRAINT uq_vapt_vupt_atendimento_cidadao_codigo UNIQUE (codigo),

    CONSTRAINT uq_vapt_vupt_atendimento_cidadao_triplo UNIQUE (vapt_vupt_atendimento_id, id_orgao, id_servico),

    CONSTRAINT chk_vapt_vupt_atendimento_cidadao_qtd CHECK (quantidade_usuarios_atendidas >= 0),

    CONSTRAINT fk_vvacc_atendimento FOREIGN KEY (vapt_vupt_atendimento_id) REFERENCES vapt_vupt_atendimento(id)

);



CREATE INDEX IF NOT EXISTS idx_vvacc_atendimento ON vapt_vupt_atendimento_cidadao(vapt_vupt_atendimento_id);

CREATE INDEX IF NOT EXISTS idx_vvacc_id_orgao ON vapt_vupt_atendimento_cidadao(id_orgao);

CREATE INDEX IF NOT EXISTS idx_vvacc_id_servico ON vapt_vupt_atendimento_cidadao(id_servico);

