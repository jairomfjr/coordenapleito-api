-- Schema do Coordenapleito: usuário, grupo, permissão e auditoria Envers.
-- DDL alinhado ao metadata do Hibernate 6.5 (ddl-auto=none).

create sequence grupo_id_seq start with 1 increment by 1;
create sequence permissao_id_seq start with 1 increment by 1;
create sequence revinfo_seq start with 1 increment by 50;
create sequence usuario_id_seq start with 1 increment by 1;

create table grupo (
    ativo boolean,
    data_atualizacao timestamp(6) with time zone,
    data_cadastro timestamp(6) with time zone,
    id bigint not null,
    codigo uuid,
    nome varchar(255),
    primary key (id)
);

create table grupo_aud (
    ativo boolean,
    rev integer not null,
    revtype smallint,
    id bigint not null,
    nome varchar(255),
    primary key (rev, id)
);

create table grupo_permissao (
    grupo_id bigint not null,
    permissao_id bigint not null,
    primary key (grupo_id, permissao_id)
);

create table grupo_permissao_aud (
    rev integer not null,
    revtype smallint,
    grupo_id bigint not null,
    permissao_id bigint not null,
    primary key (rev, grupo_id, permissao_id)
);

create table permissao (
    ativo boolean,
    ordem integer,
    sistema boolean,
    data_atualizacao timestamp(6) with time zone,
    data_cadastro timestamp(6) with time zone,
    id bigint not null,
    codigo uuid,
    acao varchar(60),
    modulo varchar(60),
    recurso varchar(60),
    chave varchar(120),
    descricao varchar(255),
    nome varchar(255),
    primary key (id)
);

create table permissao_aud (
    ativo boolean,
    ordem integer,
    rev integer not null,
    revtype smallint,
    sistema boolean,
    id bigint not null,
    acao varchar(60),
    modulo varchar(60),
    recurso varchar(60),
    chave varchar(120),
    descricao varchar(255),
    nome varchar(255),
    primary key (rev, id)
);

create table revinfo (
    id integer not null,
    data_cadastro timestamp(6) with time zone,
    timestamp bigint not null,
    login_usuario varchar(255),
    nome_usuario varchar(255),
    primary key (id)
);

create table usuario (
    ativo boolean,
    recebe_email boolean,
    data_atualizacao timestamp(6) with time zone,
    data_cadastro timestamp(6) with time zone,
    data_nascimento timestamp(6) with time zone,
    id bigint not null,
    codigo uuid,
    cargo varchar(255),
    celular varchar(255),
    cpf varchar(255),
    email varchar(255),
    nome varchar(255),
    senha varchar(255),
    telefone varchar(255),
    primary key (id)
);

create table usuario_aud (
    ativo boolean,
    recebe_email boolean,
    rev integer not null,
    revtype smallint,
    data_nascimento timestamp(6) with time zone,
    id bigint not null,
    cargo varchar(255),
    celular varchar(255),
    cpf varchar(255),
    email varchar(255),
    nome varchar(255),
    senha varchar(255),
    telefone varchar(255),
    primary key (rev, id)
);

create table usuario_grupo (
    grupo_id bigint not null,
    usuario_id bigint not null
);

create table usuario_grupo_aud (
    rev integer not null,
    revtype smallint,
    grupo_id bigint not null,
    usuario_id bigint not null,
    primary key (rev, grupo_id, usuario_id)
);

alter table grupo_aud
    add constraint fk_grupo_aud_rev
    foreign key (rev) references revinfo;

alter table grupo_permissao
    add constraint fk_grupo_permissao_permissao
    foreign key (permissao_id) references permissao;

alter table grupo_permissao
    add constraint fk_grupo_permissao_grupo
    foreign key (grupo_id) references grupo;

alter table grupo_permissao_aud
    add constraint fk_grupo_permissao_aud_rev
    foreign key (rev) references revinfo;

alter table permissao_aud
    add constraint fk_permissao_aud_rev
    foreign key (rev) references revinfo;

alter table usuario_aud
    add constraint fk_usuario_aud_rev
    foreign key (rev) references revinfo;

alter table usuario_grupo
    add constraint fk_usuario_grupo_grupo
    foreign key (grupo_id) references grupo;

alter table usuario_grupo
    add constraint fk_usuario_grupo_usuario
    foreign key (usuario_id) references usuario;

alter table usuario_grupo_aud
    add constraint fk_usuario_grupo_aud_rev
    foreign key (rev) references revinfo;
