-- liquibase formatted sql

-- changeset rmachicm:actualizar-catalogos
update as_catalogos
set ca_valor = 'Usuario Interno'
where ca_id = 16;

alter table as_catalogos
    add column ca_estado varchar(1) default 'A';

update as_catalogos
set ca_estado = 'I'
where ca_id = 17;