-- Autor da mensagem (excluído do inbox/sino para o próprio criador). Legado: NULL = todos veem.

ALTER TABLE mensagem
    ADD COLUMN IF NOT EXISTS usuario_criador_id BIGINT NULL;

ALTER TABLE mensagem
    DROP CONSTRAINT IF EXISTS fk_mensagem_usuario_criador;

ALTER TABLE mensagem
    ADD CONSTRAINT fk_mensagem_usuario_criador
        FOREIGN KEY (usuario_criador_id) REFERENCES usuario (id);

CREATE INDEX IF NOT EXISTS idx_mensagem_usuario_criador ON mensagem (usuario_criador_id);
