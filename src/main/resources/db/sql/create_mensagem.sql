-- Mensagens institucionais (CRUD admin) e leituras por usuário (inbox / sino)

CREATE SEQUENCE IF NOT EXISTS mensagem_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS mensagem_leitura_id_seq START 1;

CREATE TABLE IF NOT EXISTS mensagem (
    id BIGINT NOT NULL DEFAULT nextval('mensagem_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    titulo VARCHAR(500) NOT NULL,
    corpo TEXT NOT NULL,
    publicada BOOLEAN NOT NULL DEFAULT false,
    data_publicacao TIMESTAMPTZ,
    usuario_criador_id BIGINT NULL,
    CONSTRAINT pk_mensagem PRIMARY KEY (id),
    CONSTRAINT uq_mensagem_codigo UNIQUE (codigo),
    CONSTRAINT fk_mensagem_usuario_criador FOREIGN KEY (usuario_criador_id) REFERENCES usuario (id)
);

CREATE TABLE IF NOT EXISTS mensagem_leitura (
    id BIGINT NOT NULL DEFAULT nextval('mensagem_leitura_id_seq'),
    codigo UUID NOT NULL DEFAULT gen_random_uuid(),
    data_cadastro TIMESTAMPTZ,
    data_atualizacao TIMESTAMPTZ,
    mensagem_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    lida_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_mensagem_leitura PRIMARY KEY (id),
    CONSTRAINT uq_mensagem_leitura_msg_usuario UNIQUE (mensagem_id, usuario_id),
    CONSTRAINT fk_mensagem_leitura_mensagem FOREIGN KEY (mensagem_id) REFERENCES mensagem(id) ON DELETE CASCADE,
    CONSTRAINT fk_mensagem_leitura_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_mensagem_leitura_usuario ON mensagem_leitura(usuario_id);
CREATE INDEX IF NOT EXISTS idx_mensagem_publicada ON mensagem(publicada);
