-- Índices para acelerar agregações do dashboard VAPT VUPT (PostgreSQL).

CREATE INDEX IF NOT EXISTS idx_vapt_vupt_atendimento_data_hora
    ON vapt_vupt_atendimento (data_hora_atendimento)
    WHERE data_hora_atendimento IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_vapt_vupt_atendimento_ano_mes_equipamento
    ON vapt_vupt_atendimento (ano, mes, equipamento_id);

CREATE INDEX IF NOT EXISTS idx_vvacc_atendimento_orgao_servico
    ON vapt_vupt_atendimento_cidadao (vapt_vupt_atendimento_id, orgao_vapt_vupt_id, servico_vapt_vupt_id);
