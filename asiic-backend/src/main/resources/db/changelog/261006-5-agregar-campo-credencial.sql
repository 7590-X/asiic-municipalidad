-- liquibase formatted sql

-- changeset rmachicm:agregar-campo-credencial
alter table as_usuarios add column us_credencial varchar(10);