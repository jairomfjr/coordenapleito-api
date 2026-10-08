-- Extensão necessária para busca por nome/e-mail na listagem de cidadãos (insensível a acentos).
-- Execute uma vez no banco (usuário com permissão CREATE; em RDS pode exigir parâmetro ou suporte).
CREATE EXTENSION IF NOT EXISTS unaccent;
