ALTER TABLE public.cidadao
    ADD COLUMN IF NOT EXISTS sexo VARCHAR(30) NOT NULL DEFAULT 'NAO_INFORMADO',
    ADD COLUMN IF NOT EXISTS genero_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS orientacao_sexual_id BIGINT NULL;

ALTER TABLE public.cidadao
    ADD CONSTRAINT fk_cidadao_genero
        FOREIGN KEY (genero_id) REFERENCES genero (id);

ALTER TABLE public.cidadao
    ADD CONSTRAINT fk_cidadao_orientacao_sexual
        FOREIGN KEY (orientacao_sexual_id) REFERENCES orientacao_sexual (id);

CREATE INDEX IF NOT EXISTS ix_cidadao_genero_id ON public.cidadao (genero_id);
CREATE INDEX IF NOT EXISTS ix_cidadao_orientacao_sexual_id ON public.cidadao (orientacao_sexual_id);
