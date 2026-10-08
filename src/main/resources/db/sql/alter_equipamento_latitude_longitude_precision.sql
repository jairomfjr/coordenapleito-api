-- Aumenta precisão das coordenadas para evitar arredondamento em 6 casas decimais.
ALTER TABLE public.equipamento ALTER COLUMN latitude TYPE NUMERIC(19, 10);
ALTER TABLE public.equipamento ALTER COLUMN longitude TYPE NUMERIC(19, 10);
