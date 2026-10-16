insert into as_tablas (ta_id, ta_nombre) values (11,'as_grupo_usuario');
call sp_agregar_catalogo(11,'Usuario Externo','UEXT');
call sp_agregar_catalogo(11,'Usuario Interno','UINT');
update as_catalogos set ca_pk_externa = 'role_vecino' where ca_seudo = 'RVECO';
update as_catalogos set ca_estado = 'A' where ca_seudo = 'ROPER';
