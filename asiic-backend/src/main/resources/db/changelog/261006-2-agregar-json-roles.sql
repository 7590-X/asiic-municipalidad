-- liquibase formatted sql

-- changeset rmachicm:agregar-json-roles
alter table as_usuarios add column us_json_roles jsonb;