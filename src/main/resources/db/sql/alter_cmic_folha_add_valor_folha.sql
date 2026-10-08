-- Valor total da folha CMIC (nível competência). Bases já existentes: adiciona coluna com default zero.
ALTER TABLE cmic_folha
    ADD COLUMN IF NOT EXISTS valor_folha NUMERIC(19, 2) NOT NULL DEFAULT 0;
