-- Migração: período (mês/ano) em equipamento_servico e unicidade por (equipamento, serviço, mês, ano).
-- Em bases criadas pelo Hibernate, a UNIQUE antiga em (equipamento_id, servico_id) pode ter nome
-- automático (ex.: uk96s9wiuhvq7h3f4nu8bi9qmpi); por isso removemos por definição, não só por nome fixo.

ALTER TABLE equipamento_servico
    ADD COLUMN IF NOT EXISTS mes INTEGER,
    ADD COLUMN IF NOT EXISTS ano INTEGER;

UPDATE equipamento_servico
SET mes = EXTRACT(MONTH FROM CURRENT_DATE)::INTEGER
WHERE mes IS NULL;

UPDATE equipamento_servico
SET ano = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER
WHERE ano IS NULL;

ALTER TABLE equipamento_servico
    ALTER COLUMN mes SET NOT NULL,
    ALTER COLUMN ano SET NOT NULL;

-- Remove UNIQUE antiga apenas em (equipamento_id, servico_id), sem mes/ano (qualquer nome).
DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT c.conname
    FROM pg_constraint c
    JOIN pg_class t ON c.conrelid = t.oid
    WHERE t.relname = 'equipamento_servico'
      AND c.contype = 'u'
      AND pg_get_constraintdef(c.oid) LIKE '%equipamento_id%'
      AND pg_get_constraintdef(c.oid) LIKE '%servico_id%'
      AND pg_get_constraintdef(c.oid) NOT LIKE '%mes%'
  LOOP
    EXECUTE format('ALTER TABLE equipamento_servico DROP CONSTRAINT %I', r.conname);
  END LOOP;
END $$;

ALTER TABLE equipamento_servico
    DROP CONSTRAINT IF EXISTS uq_equipamento_servico_vinculo;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON c.conrelid = t.oid
    WHERE t.relname = 'equipamento_servico'
      AND c.contype = 'u'
      AND c.conname = 'uq_equipamento_servico_vinculo'
  ) THEN
    ALTER TABLE equipamento_servico
      ADD CONSTRAINT uq_equipamento_servico_vinculo UNIQUE (equipamento_id, servico_id, mes, ano);
  END IF;
END $$;
