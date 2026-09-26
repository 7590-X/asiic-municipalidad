-- liquibase formatted sql

-- changeset rmachicm:agregar-columna-descripcion-para-locacion
alter table as_locaciones add column lo_descripcion varchar(45);