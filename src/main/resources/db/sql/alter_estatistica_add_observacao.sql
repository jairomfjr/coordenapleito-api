ALTER TABLE public.estatistica
    ADD COLUMN IF NOT EXISTS observacao VARCHAR(1000);
