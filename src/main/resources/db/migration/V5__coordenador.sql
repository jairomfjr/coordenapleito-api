-- Coordenador + permissões funcionais do recurso coordenador.

create sequence coordenador_id_seq start with 1 increment by 1;

create table coordenador (
    data_atualizacao timestamp(6) with time zone,
    data_cadastro timestamp(6) with time zone,
    id bigint not null,
    codigo uuid,
    nome varchar(255) not null,
    cpf varchar(11) not null,
    telefone varchar(20) not null,
    email varchar(255) not null,
    local_trabalho_id bigint not null,
    local_votacao_id bigint not null,
    primary key (id),
    constraint uk_coordenador_cpf unique (cpf)
);

alter table coordenador
    add constraint fk_coordenador_local_trabalho
    foreign key (local_trabalho_id) references local_votacao;

alter table coordenador
    add constraint fk_coordenador_local_votacao
    foreign key (local_votacao_id) references local_votacao;

create table coordenador_aud (
    rev integer not null,
    revtype smallint,
    id bigint not null,
    nome varchar(255),
    cpf varchar(11),
    telefone varchar(20),
    email varchar(255),
    local_trabalho_id bigint,
    local_votacao_id bigint,
    primary key (rev, id)
);

alter table coordenador_aud
    add constraint fk_coordenador_aud_rev
    foreign key (rev) references revinfo;

insert into permissao (id, codigo, data_cadastro, data_atualizacao, nome, descricao, ativo,
                       chave, modulo, recurso, acao, ordem, sistema)
select nextval('permissao_id_seq'), gen_random_uuid(), now(), now(), v.chave, v.descricao, true,
       v.chave, 'pleito', 'coordenador', v.acao, v.ordem, true
from (values
    ('coordenador.menu', 'Menu — Coordenadores', 'menu', 120),
    ('coordenador.pagina', 'Página — Coordenadores', 'pagina', 121),
    ('coordenador.listar', 'Listar — Coordenadores', 'listar', 122),
    ('coordenador.visualizar', 'Visualizar — Coordenadores', 'visualizar', 123),
    ('coordenador.criar', 'Criar — Coordenadores', 'criar', 124),
    ('coordenador.editar', 'Editar — Coordenadores', 'editar', 125),
    ('coordenador.excluir', 'Excluir — Coordenadores', 'excluir', 126)
) as v(chave, descricao, acao, ordem)
where not exists (select 1 from permissao p where p.chave = v.chave);

insert into grupo_permissao (grupo_id, permissao_id)
select g.id, p.id
from grupo g
cross join permissao p
where g.ativo = true
  and g.nome in ('Analista', 'Administrador')
  and p.chave like 'coordenador.%'
  and not exists (
      select 1 from grupo_permissao gp
      where gp.grupo_id = g.id and gp.permissao_id = p.id
  );
