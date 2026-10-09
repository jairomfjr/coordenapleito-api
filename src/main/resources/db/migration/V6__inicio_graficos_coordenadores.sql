-- Permissão opt-in: gráficos de coordenadores na página inicial.
-- Habilite na árvore do grupo. Não atribui automaticamente.

insert into permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo,
                       chave, modulo, recurso, acao, ordem, sistema)
select nextval('permissao_id_seq'), gen_random_uuid(), now(), now(),
       'inicio.graficos-coordenadores',
       'Gráficos de coordenadores na página inicial',
       true,
       'inicio.graficos-coordenadores', 'geral', 'inicio', 'graficos-coordenadores', 4, true
where not exists (
    select 1 from permissao p where p.chave = 'inicio.graficos-coordenadores'
);
