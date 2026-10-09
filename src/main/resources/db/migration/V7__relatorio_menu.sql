-- Relatórios do Coordena Pleito (menu/página). Opt-in: replica quem já tinha o item legado.

insert into permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo,
                       chave, modulo, recurso, acao, ordem, sistema)
select nextval('permissao_id_seq'), gen_random_uuid(), now(), now(),
       v.chave, v.descricao, true, v.chave, 'relatorios', 'relatorio', v.acao, v.ordem, true
from (values
    ('relatorio.menu', 'Menu — Relatórios', 'menu', 200),
    ('relatorio.pagina', 'Página — Relatórios', 'pagina', 201),
    ('relatorio.listar', 'Listar — Relatórios', 'listar', 202),
    ('relatorio.visualizar', 'Visualizar — Relatórios', 'visualizar', 203),
    ('relatorio.gerar', 'Gerar PDF — Relatórios', 'gerar', 204)
) as v(chave, descricao, acao, ordem)
where not exists (select 1 from permissao p where p.chave = v.chave);

insert into grupo_permissao (grupo_id, permissao_id)
select gp.grupo_id, nova.id
from grupo_permissao gp
join permissao legado on legado.id = gp.permissao_id
    and legado.chave = 'relatorio-dashboard.gerar-qualificacao'
join permissao nova on nova.chave in (
    'relatorio.menu', 'relatorio.pagina', 'relatorio.listar', 'relatorio.visualizar', 'relatorio.gerar'
)
where not exists (
    select 1 from grupo_permissao ja
    where ja.grupo_id = gp.grupo_id and ja.permissao_id = nova.id
);

update permissao
set ativo = false,
    data_atualizacao = now()
where chave = 'relatorio-dashboard.gerar-qualificacao'
  and ativo = true;
