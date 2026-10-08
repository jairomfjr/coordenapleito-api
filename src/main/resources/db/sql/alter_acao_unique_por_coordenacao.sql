-- Unicidade da ação passa a ser por coordenação: mesma descrição pode existir em coordenações diferentes.
-- Remove unique apenas em descricao e cria unique em (descricao, coordenacao_id).
ALTER TABLE public.acao
    DROP CONSTRAINT IF EXISTS uq_acao_descricao;

ALTER TABLE public.acao
    ADD CONSTRAINT uq_acao_descricao_coordenacao UNIQUE (descricao, coordenacao_id);
