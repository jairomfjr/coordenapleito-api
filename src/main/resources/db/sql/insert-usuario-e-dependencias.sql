-- =============================================================================
-- Script: Inserir usuário e objetos que ele depende
-- Banco: PostgreSQL
-- Uso: Ajuste os valores nas variáveis abaixo e execute o script.
--      A senha deve ser um hash BCrypt (ex: gerado pela aplicação ou
--      uso de senha '123456' -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy).
-- =============================================================================

-- Parâmetros (ajuste conforme necessário)
-- Para obter um hash BCrypt para outra senha, use a API ou um gerador BCrypt.
DO $$
DECLARE
  -- IDs obtidos após os inserts (preenchidos no script)
  v_estado_id     BIGINT;
  v_municipio_id  BIGINT;
  v_bairro_id     BIGINT;
  v_permissao_id  BIGINT;
  v_grupo_id      BIGINT;
  v_usuario_id     BIGINT;
  v_usuario_codigo UUID;
  -- Dados do usuário (edite aqui)
  v_usuario_nome   TEXT := 'Usuário Exemplo';
  v_usuario_cpf    TEXT := '529.982.247-25';  -- CPF válido para teste
  v_usuario_senha  TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';  -- BCrypt para "123456"
  v_usuario_email  TEXT := 'usuario@exemplo.gov.br';
  v_usuario_tel    TEXT := '(85) 99999-9999';
  v_usuario_cargo  TEXT := 'Analista';
  v_logradouro     TEXT := 'Rua Exemplo';
  v_logradouro_num TEXT := '100';
  v_complemento    TEXT := 'Sala 1';
  v_cep            TEXT := '60000-000';
  v_grupo_nome     TEXT := 'Administrador';
  v_permissao_nome TEXT := 'ROLE_ADMINISTRADOR';
BEGIN
  -- -------------------------------------------------------------------------
  -- 1. Estado (dependência de Municipio)
  -- -------------------------------------------------------------------------
  SELECT id INTO v_estado_id FROM estado WHERE sigla = 'CE' LIMIT 1;
  IF v_estado_id IS NULL THEN
    INSERT INTO estado (id, codigo, data_cadastro, data_atualizacao, nome, ibge, sigla)
    VALUES (nextval('estado_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Ceará', '23', 'CE')
    RETURNING id INTO v_estado_id;
  END IF;

  -- -------------------------------------------------------------------------
  -- 2. Municipio (dependência de Bairro)
  -- -------------------------------------------------------------------------
  SELECT id INTO v_municipio_id FROM municipio WHERE estado_id = v_estado_id AND nome = 'Fortaleza' LIMIT 1;
  IF v_municipio_id IS NULL THEN
    INSERT INTO municipio (id, codigo, data_cadastro, data_atualizacao, nome, codigoibge, estado_id)
    VALUES (nextval('municipio_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Fortaleza', 2304400, v_estado_id)
    RETURNING id INTO v_municipio_id;
  END IF;

  -- -------------------------------------------------------------------------
  -- 3. Bairro (dependência de Usuario.Endereco)
  -- -------------------------------------------------------------------------
  SELECT id INTO v_bairro_id FROM bairro WHERE municipio_id = v_municipio_id AND nome = 'Centro' LIMIT 1;
  IF v_bairro_id IS NULL THEN
    INSERT INTO bairro (id, codigo, data_cadastro, data_atualizacao, nome, municipio_id)
    VALUES (nextval('bairro_id_seq'), gen_random_uuid(), NOW(), NOW(), 'Centro', v_municipio_id)
    RETURNING id INTO v_bairro_id;
  END IF;

  -- -------------------------------------------------------------------------
  -- 4. Permissao (dependência de Grupo)
  -- -------------------------------------------------------------------------
  SELECT id INTO v_permissao_id FROM permissao WHERE nome = v_permissao_nome LIMIT 1;
  IF v_permissao_id IS NULL THEN
    INSERT INTO permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo)
    VALUES (nextval('permissao_id_seq'), gen_random_uuid(), NOW(), NOW(), v_permissao_nome, 'Permissão administrativa', TRUE)
    RETURNING id INTO v_permissao_id;
  END IF;

  -- -------------------------------------------------------------------------
  -- 5. Grupo e grupo_permissao
  -- -------------------------------------------------------------------------
  SELECT id INTO v_grupo_id FROM grupo WHERE nome = v_grupo_nome LIMIT 1;
  IF v_grupo_id IS NULL THEN
    INSERT INTO grupo (id, codigo, data_cadastro, data_atualizacao, nome, ativo)
    VALUES (nextval('grupo_id_seq'), gen_random_uuid(), NOW(), NOW(), v_grupo_nome, TRUE)
    RETURNING id INTO v_grupo_id;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM grupo_permissao WHERE grupo_id = v_grupo_id AND permissao_id = v_permissao_id) THEN
    INSERT INTO grupo_permissao (grupo_id, permissao_id) VALUES (v_grupo_id, v_permissao_id);
  END IF;

  -- -------------------------------------------------------------------------
  -- 6. Usuario (depende de Bairro; dados de Pessoa + Usuario)
  -- -------------------------------------------------------------------------
  IF NOT EXISTS (SELECT 1 FROM usuario WHERE cpf = v_usuario_cpf) THEN
    v_usuario_codigo := gen_random_uuid();
    INSERT INTO usuario (
      id, codigo, data_cadastro, data_atualizacao,
      nome, data_nascimento, cpf,
      bairro_id, logradouro, logradouro_numero, complemento, cep,
      telefone, email,
      senha, cargo, recebe_email, ativo
    ) VALUES (
      nextval('usuario_id_seq'), v_usuario_codigo, NOW(), NOW(),
      v_usuario_nome, '1990-01-01'::timestamp with time zone, v_usuario_cpf,
      v_bairro_id, v_logradouro, v_logradouro_num, v_complemento, v_cep,
      v_usuario_tel, v_usuario_email,
      v_usuario_senha, v_usuario_cargo, TRUE, TRUE
    )
    RETURNING id INTO v_usuario_id;

    -- -------------------------------------------------------------------------
    -- 7. usuario_grupo (vínculo usuário x grupo)
    -- -------------------------------------------------------------------------
    INSERT INTO usuario_grupo (usuario_id, grupo_id)
    VALUES (v_usuario_id, v_grupo_id);

    RAISE NOTICE 'Usuário inserido com id=% e codigo=%', v_usuario_id, v_usuario_codigo;
  ELSE
    RAISE NOTICE 'Usuário com CPF % já existe. Nenhum insert de usuário realizado.', v_usuario_cpf;
  END IF;
END $$;
