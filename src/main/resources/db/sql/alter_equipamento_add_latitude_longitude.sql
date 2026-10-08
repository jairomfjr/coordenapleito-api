-- Adiciona colunas de coordenadas na tabela equipamento (mesmo molde do município)
ALTER TABLE public.equipamento ADD COLUMN IF NOT EXISTS latitude NUMERIC(19, 6);
ALTER TABLE public.equipamento ADD COLUMN IF NOT EXISTS longitude NUMERIC(19, 6);
