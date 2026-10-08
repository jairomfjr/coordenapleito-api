-- Ligação Atividade -> Equipamento (N:1, opcional)
ALTER TABLE atividade ADD COLUMN IF NOT EXISTS equipamento_id BIGINT NULL REFERENCES equipamento(id);
CREATE INDEX IF NOT EXISTS idx_atividade_equipamento_id ON atividade(equipamento_id);
