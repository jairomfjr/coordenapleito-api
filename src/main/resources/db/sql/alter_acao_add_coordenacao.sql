-- Vincula ação à coordenação. Coordenação opcional para não quebrar dados existentes.
ALTER TABLE public.acao
    ADD COLUMN IF NOT EXISTS coordenacao_id BIGINT NULL,
    ADD CONSTRAINT fk_acao_coordenacao FOREIGN KEY (coordenacao_id) REFERENCES coordenacao (id);

CREATE INDEX IF NOT EXISTS ix_acao_coordenacao_id ON public.acao (coordenacao_id);
