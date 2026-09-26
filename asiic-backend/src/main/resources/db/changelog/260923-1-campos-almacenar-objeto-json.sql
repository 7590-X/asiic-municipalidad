-- liquibase formatted sql

-- changeset rmachicm:nuevo-campo-para-manejo-json-no-estructurado
alter table as_insidencias add column in_jsons jsonb;