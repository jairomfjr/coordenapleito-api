ALTER TABLE equipamento_servico
    ADD COLUMN IF NOT EXISTS categoria_id BIGINT,
    ADD COLUMN IF NOT EXISTS tipo_servico_id BIGINT;

ALTER TABLE equipamento_servico
    ADD CONSTRAINT fk_equipamento_servico_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id);

ALTER TABLE equipamento_servico
    ADD CONSTRAINT fk_equipamento_servico_tipo_servico
        FOREIGN KEY (tipo_servico_id) REFERENCES tipo_servico(id);

-- Preencher dados existentes com base no serviço vinculado
UPDATE equipamento_servico es
SET
    tipo_servico_id = s.tipo_servico_id,
    categoria_id = ts.categoria_id
FROM servico s
JOIN tipo_servico ts ON ts.id = s.tipo_servico_id
WHERE es.servico_id = s.id
  AND (es.tipo_servico_id IS NULL OR es.categoria_id IS NULL);
