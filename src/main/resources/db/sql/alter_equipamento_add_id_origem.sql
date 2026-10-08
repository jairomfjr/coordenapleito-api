-- Campo opcional para integração com sistemas legados/origem externa.
ALTER TABLE public.equipamento
    ADD COLUMN IF NOT EXISTS id_origem BIGINT NULL;

CREATE INDEX IF NOT EXISTS ix_equipamento_id_origem ON public.equipamento (id_origem);

