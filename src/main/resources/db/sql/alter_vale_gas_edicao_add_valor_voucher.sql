-- Valor do voucher da edição Vale Gás (nível competência). Bases já existentes: adiciona coluna com default zero.
ALTER TABLE vale_gas_edicao
    ADD COLUMN IF NOT EXISTS valor_voucher NUMERIC(19, 2) NOT NULL DEFAULT 0;
