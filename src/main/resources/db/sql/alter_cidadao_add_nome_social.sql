-- Nome social (opcional), conforme políticas de identidade.

ALTER TABLE cidadao ADD COLUMN IF NOT EXISTS nome_social VARCHAR(255);
