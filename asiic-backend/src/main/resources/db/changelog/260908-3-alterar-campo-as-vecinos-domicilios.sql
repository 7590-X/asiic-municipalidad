-- liquibase formatted sql
-- changeset rmachicm:Alterar columna vd_condicion para que permita nulls
alter table as_vecinos_domicilios alter column  vd_condicion drop not null;