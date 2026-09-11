-- liquibase formatted sql

-- changeset rmachicm:catalogos-tipos-usuarios
insert into as_tablas (ta_id, ta_nombre) values (5,'as_tipo_usuario');
call sp_agregar_catalogo(5,'Vecino','RVECO');
call sp_agregar_catalogo(5,'Analista','RANTA');
call sp_agregar_catalogo(5,'Operador','ROPER');