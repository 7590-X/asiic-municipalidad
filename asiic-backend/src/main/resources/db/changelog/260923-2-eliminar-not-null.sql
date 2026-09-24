-- liquibase formatted sql

-- changeset rmachicm:eliminar-campos-obligatorios
alter table as_insidencias rename to as_incidencias;
alter table as_incidencias alter column in_propuesta drop not null;
alter table as_incidencias alter column in_tipo_servicio drop not null;