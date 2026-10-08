-- Vincula equipamento à coordenação. Coordenação opcional para não quebrar dados existentes.
ALTER TABLE public.equipamento
    ADD COLUMN IF NOT EXISTS coordenacao_id BIGINT NULL,
    ADD CONSTRAINT fk_equipamento_coordenacao FOREIGN KEY (coordenacao_id) REFERENCES coordenacao (id);

CREATE INDEX IF NOT EXISTS ix_equipamento_coordenacao_id ON public.equipamento (coordenacao_id);
