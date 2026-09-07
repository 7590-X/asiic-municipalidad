-- liquibase formatted sql
-- changeset rmachicm:Alteracion-tabla-as_personas-con-nit-a-13-caracteres
alter table as_personas alter column pe_nit type varchar(13);