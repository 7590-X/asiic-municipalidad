-- liquibase formatted sql

-- changeset rmachicm:nueva-tabla-rechazos
alter table as_incidencias
    add column in_rechazos numeric(1);

create table as_rechazos
(
    re_id         serial,
    re_incidencia int        not null,
    re_numero     numeric(1) not null,
    re_datos      jsonb      not null,
    foreign key (re_incidencia) references as_incidencias (in_id)
);