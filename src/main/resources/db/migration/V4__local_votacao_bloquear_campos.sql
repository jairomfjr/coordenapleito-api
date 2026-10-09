-- Permissão de bloqueio de campos do local de votação (exceto coordenadores).
-- Opt-in pela árvore do grupo: não atribui automaticamente a Analista/Administrador.

insert into permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo,
                       chave, modulo, recurso, acao, ordem, sistema)
select nextval('permissao_id_seq'), gen_random_uuid(), now(), now(),
       'local-votacao.bloquear-campos',
       'Bloquear campos — Locais de votação (exceto coordenadores)',
       true,
       'local-votacao.bloquear-campos', 'pleito', 'local-votacao', 'bloquear-campos', 87, true
where not exists (
    select 1 from permissao p where p.chave = 'local-votacao.bloquear-campos'
);

delete from grupo_permissao
where permissao_id in (
    select id from permissao where chave = 'local-votacao.bloquear-campos'
);
