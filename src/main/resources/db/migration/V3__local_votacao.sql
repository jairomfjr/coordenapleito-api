-- Local de votação + permissões funcionais do recurso local-votacao.

create sequence local_votacao_id_seq start with 1 increment by 1;

create table local_votacao (
    zona integer not null,
    qtd_secoes integer not null,
    qtd_eleitores integer not null,
    qtd_coordenadores integer not null,
    data_atualizacao timestamp(6) with time zone,
    data_cadastro timestamp(6) with time zone,
    id bigint not null,
    codigo uuid,
    local_votacao varchar(255) not null,
    endereco varchar(255) not null,
    bairro varchar(255) not null,
    primary key (id),
    constraint uk_local_votacao_zona_nome unique (zona, local_votacao)
);

create table local_votacao_aud (
    zona integer,
    qtd_secoes integer,
    qtd_eleitores integer,
    qtd_coordenadores integer,
    rev integer not null,
    revtype smallint,
    id bigint not null,
    local_votacao varchar(255),
    endereco varchar(255),
    bairro varchar(255),
    primary key (rev, id)
);

alter table local_votacao_aud
    add constraint fk_local_votacao_aud_rev
    foreign key (rev) references revinfo;

insert into permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo,
                       chave, modulo, recurso, acao, ordem, sistema)
select nextval('permissao_id_seq'), gen_random_uuid(), now(), now(), v.chave, v.descricao, true,
       v.chave, 'pleito', 'local-votacao', v.acao, v.ordem, true
from (values
    ('local-votacao.menu', 'Menu — Locais de votação', 'menu', 80),
    ('local-votacao.pagina', 'Página — Locais de votação', 'pagina', 81),
    ('local-votacao.listar', 'Listar — Locais de votação', 'listar', 82),
    ('local-votacao.visualizar', 'Visualizar — Locais de votação', 'visualizar', 83),
    ('local-votacao.criar', 'Criar — Locais de votação', 'criar', 84),
    ('local-votacao.editar', 'Editar — Locais de votação', 'editar', 85),
    ('local-votacao.excluir', 'Excluir — Locais de votação', 'excluir', 86)
) as v(chave, descricao, acao, ordem)
where not exists (select 1 from permissao p where p.chave = v.chave);

insert into grupo_permissao (grupo_id, permissao_id)
select g.id, p.id
from grupo g
cross join permissao p
where g.ativo = true
  and g.nome in ('Analista', 'Administrador')
  and p.chave like 'local-votacao.%'
  and not exists (
      select 1 from grupo_permissao gp
      where gp.grupo_id = g.id and gp.permissao_id = p.id
  );
