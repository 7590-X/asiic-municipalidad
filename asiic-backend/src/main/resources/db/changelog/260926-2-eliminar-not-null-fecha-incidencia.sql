-- liquibase formatted sql

-- changeset rmachicm:eliminar-not-null-in_direccion_tabla_as_incidencia
alter table as_incidencias alter column in_fec_insidencia drop not null;