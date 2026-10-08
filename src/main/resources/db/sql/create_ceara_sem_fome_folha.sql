-- Ceara Sem Fome: folhas de pagamento e municípios por folha com consolidação

-- Sequências
CREATE SEQUENCE IF NOT EXISTS ceara_sem_fome_folha_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS ceara_sem_fome_folha_municipio_id_seq START 1;

-- Tabela folha de pagamento (mês/ano)
CREATE TABLE IF NOT EXISTS ceara_sem_fome_folha (
    id BIGINT NOT NULL DEFAULT nextval('ceara_sem_fome_folha_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    mes INTEGER NOT NULL,
    ano INTEGER NOT NULL,
    CONSTRAINT pk_ceara_sem_fome_folha PRIMARY KEY (id),
    CONSTRAINT uq_ceara_sem_fome_folha_codigo UNIQUE (codigo),
    CONSTRAINT chk_ceara_sem_fome_folha_mes CHECK (mes >= 1 AND mes <= 12),
    CONSTRAINT chk_ceara_sem_fome_folha_ano CHECK (ano >= 1900 AND ano <= 2100)
);

-- Tabela município por folha (beneficiários e valor por município)
CREATE TABLE IF NOT EXISTS ceara_sem_fome_folha_municipio (
    id BIGINT NOT NULL DEFAULT nextval('ceara_sem_fome_folha_municipio_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    ceara_sem_fome_folha_id BIGINT NOT NULL,
    municipio_id BIGINT NOT NULL,
    quantidade_beneficiarios INTEGER NOT NULL DEFAULT 0,
    valor_beneficiarios NUMERIC(19, 2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_ceara_sem_fome_folha_municipio PRIMARY KEY (id),
    CONSTRAINT uq_ceara_sem_fome_folha_municipio_folha_municipio UNIQUE (ceara_sem_fome_folha_id, municipio_id),
    CONSTRAINT fk_ceara_sem_fome_folha_municipio_folha FOREIGN KEY (ceara_sem_fome_folha_id) REFERENCES ceara_sem_fome_folha(id) ON DELETE CASCADE,
    CONSTRAINT fk_ceara_sem_fome_folha_municipio_municipio FOREIGN KEY (municipio_id) REFERENCES municipio(id),
    CONSTRAINT chk_ceara_sem_fome_folha_municipio_qtd CHECK (quantidade_beneficiarios >= 0),
    CONSTRAINT chk_ceara_sem_fome_folha_municipio_valor CHECK (valor_beneficiarios >= 0)
);

CREATE INDEX IF NOT EXISTS idx_ceara_sem_fome_folha_municipio_folha ON ceara_sem_fome_folha_municipio(ceara_sem_fome_folha_id);
CREATE INDEX IF NOT EXISTS idx_ceara_sem_fome_folha_municipio_municipio ON ceara_sem_fome_folha_municipio(municipio_id);

