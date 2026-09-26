-- liquibase formatted sql
-- changeset rmachicm:Alterar capacidad de campos para que almacenen el UUID del usuario de KC
alter table as_correos alter column co_usr_registro type varchar(36);
alter table as_correos alter column co_usr_modifico type varchar(36);

alter table as_telefonos alter column te_usr_registro type varchar(36);

alter table as_vecinos_telefonos alter column vt_usr_registro type varchar(36);
alter table as_vecinos_telefonos alter column vt_usr_modifico type varchar(36);

alter table as_vecinos alter column ve_usr_registro type varchar(36);
alter table as_vecinos alter column ve_usr_modifico type varchar(36);

alter table as_insidencias_archivos alter column ai_usr_registro type varchar(36);
alter table as_insidencias_archivos alter column ai_usr_modifico type varchar(36);

alter table as_usuarios alter column us_id type varchar(36);
alter table as_usuarios alter column us_usr_registro type varchar(36);
alter table as_usuarios alter column us_usr_modifico type varchar(36);

alter table as_insidencias alter column in_usr_registro type varchar(36);
alter table as_insidencias alter column in_usr_modifico type varchar(36);

alter table as_documentos_investigacion alter column di_usr_registro type varchar(36);
alter table as_documentos_investigacion alter column di_usr_modifico type varchar(36);

alter table as_investigaciones_campo alter column  ic_usr_registro type varchar(36);
alter table as_investigaciones_campo alter column  ic_usr_modifico type varchar(36);