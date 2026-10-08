-- VAPT VUPT: atendimentos por equipamento (ano, mês, data/hora)

CREATE SEQUENCE IF NOT EXISTS vapt_vupt_atendimento_id_seq START 1;

CREATE TABLE IF NOT EXISTS vapt_vupt_atendimento (
    id BIGINT NOT NULL DEFAULT nextval('vapt_vupt_atendimento_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    data_hora_atendimento TIMESTAMP NOT NULL,
    equipamento_id BIGINT NOT NULL,
    CONSTRAINT pk_vapt_vupt_atendimento PRIMARY KEY (id),
    CONSTRAINT uq_vapt_vupt_atendimento_codigo UNIQUE (codigo),
    CONSTRAINT chk_vapt_vupt_atendimento_mes CHECK (mes >= 1 AND mes <= 12),
    CONSTRAINT chk_vapt_vupt_atendimento_ano CHECK (ano >= 1900 AND ano <= 2100),
    CONSTRAINT fk_vapt_vupt_atendimento_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamento(id)
);

CREATE INDEX IF NOT EXISTS idx_vapt_vupt_atendimento_equipamento ON vapt_vupt_atendimento(equipamento_id);
CREATE INDEX IF NOT EXISTS idx_vapt_vupt_atendimento_ano_mes ON vapt_vupt_atendimento(ano DESC, mes DESC);
