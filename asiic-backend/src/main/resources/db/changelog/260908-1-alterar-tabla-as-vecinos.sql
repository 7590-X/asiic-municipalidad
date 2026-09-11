-- liquibase formatted sql
-- changeset rmachicm:Alterar tabla as_vecinos, agregar campo para registrar el codigo de cuenta
alter table as_vecinos add column ve_usr_registro varchar(25);
update as_vecinos set ve_usr_registro = '-1';
alter table as_vecinos alter column ve_usr_registro set not null;