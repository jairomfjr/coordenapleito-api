CREATE SEQUENCE IF NOT EXISTS organograma_id_seq START 1;

CREATE TABLE IF NOT EXISTS organograma (
    id BIGINT NOT NULL DEFAULT nextval('organograma_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(120) NOT NULL,
    descricao VARCHAR(500),
    sigla VARCHAR(60),
    ordem INTEGER NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    parent_id BIGINT NULL,
    foto_caminho VARCHAR(500),
    foto_content_type VARCHAR(100),
    CONSTRAINT pk_organograma PRIMARY KEY (id),
    CONSTRAINT uq_organograma_codigo UNIQUE (codigo),
    CONSTRAINT fk_organograma_parent FOREIGN KEY (parent_id) REFERENCES organograma(id)
);

CREATE INDEX IF NOT EXISTS idx_organograma_parent ON organograma(parent_id);
CREATE INDEX IF NOT EXISTS idx_organograma_nome ON organograma(nome);
CREATE INDEX IF NOT EXISTS idx_organograma_tipo ON organograma(tipo);
