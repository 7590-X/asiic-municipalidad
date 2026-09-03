-- liquibase formatted sql
-- changeset rmachicm:nueva-columna-para-seudonimos-de-catalogos

alter table as_catalogos
    add column ca_seudo varchar(6) unique;

update as_catalogos set ca_seudo = 'PEIN' where ca_id = 8;