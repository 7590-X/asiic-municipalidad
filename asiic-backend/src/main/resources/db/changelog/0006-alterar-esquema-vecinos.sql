-- liquibase formatted sql
-- changeset rmachicm:alterar-esquema-vecinos

alter table as_vecinos drop column ve_direccion;

alter table as_direcciones drop column di_tipo;
alter table as_direcciones add column di_tipo smallint null references as_catalogos(ca_id)