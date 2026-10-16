-- liquibase formatted sql

-- changeset rmachicm:nueva-tabla-munis-usuarios
create table as_munis_usuarios(
    mu_muni         smallint,
    mu_usuario      varchar(36),
    mu_estado       varchar(1),
    mu_fec_registro timestamp,
    mu_usr_registro varchar(36),
    mu_fec_modifico timestamp,
    mu_usr_modifico varchar(36),
    primary key (mu_muni, mu_usuario),
    foreign key (mu_muni) references as_munis(mu_id),
    foreign key (mu_usuario) references as_usuarios(us_id)
);