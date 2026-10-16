-- liquibase formatted sql

-- changeset rmachicm:modificacion-estructura-usuarios
alter table as_usuarios
    add column us_correo int null;

alter table as_usuarios
    add foreign key (us_correo) references as_correos (co_id);

update as_usuarios
set us_correo = (select ve_correo
                 from as_vecinos
                 where ve_id = us_persona);
