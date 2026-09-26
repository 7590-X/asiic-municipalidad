-- liquibase formatted sql

-- changeset asiic:catalogos-incidencias
-- 1. Dependencias (6)
insert into as_tablas (ta_id, ta_nombre) values (6,'as_dependencias');
call sp_agregar_catalogo(6, 'Policía Municipal de Tránsito', 'PMT');
call sp_agregar_catalogo(6, 'Empresa Municipal de Agua', 'EMPAGU');
call sp_agregar_catalogo(6, 'Juzgado de Asuntos Municipales', 'JAM');
call sp_agregar_catalogo(6, 'Obras Públicas', 'OBRAS');

-- 2. Tipos de Servicio (7)
insert into as_tablas (ta_id, ta_nombre) values (7,'as_tipos_servicio');
call sp_agregar_catalogo(7, 'Servicio de Agua', 'AGUA');
call sp_agregar_catalogo(7, 'Impuesto Único Sobre Inmuebles (IUSI)', 'IUSI');
call sp_agregar_catalogo(7, 'Tren de Aseo', 'ASEO');
call sp_agregar_catalogo(7, 'Alumbrado Público', 'ALUMB');

-- 3. Tipos de Denuncia (8)
insert into as_tablas (ta_id, ta_nombre) values (8,'as_tipos_denuncia');
call sp_agregar_catalogo(8, 'Construcción sin licencia', 'CONIL');
call sp_agregar_catalogo(8, 'Tala ilegal de árboles', 'TALA');
call sp_agregar_catalogo(8, 'Contaminación sónica', 'RUIDO');
call sp_agregar_catalogo(8, 'Venta de licor fuera de horario', 'LICOR');

-- 4. Áreas (9)
insert into as_tablas (ta_id, ta_nombre) values (9,'as_areas');
call sp_agregar_catalogo(9, 'Tránsito y Vialidad', 'TRAN');
call sp_agregar_catalogo(9, 'Limpieza y Ornato', 'ORN');
call sp_agregar_catalogo(9, 'Seguridad Ciudadana', 'SEG');
call sp_agregar_catalogo(9, 'Parques y Recreación', 'PARQ');
call sp_agregar_catalogo(9, 'Servicios Públicos', 'SERV');

-- 5. Privacidad (10)
insert into as_tablas (ta_id, ta_nombre) values (10,'as_privacidad');
call sp_agregar_catalogo(10, 'Público', 'PUB');
call sp_agregar_catalogo(10, 'Privado', 'PRIV');

-- 6. Tipos de Incidencia
INSERT INTO as_tipo_insidencia (ti_id, ti_nombre, ti_descripcion, ti_estado, ti_fec_registro)
VALUES (1, 'Queja', 'Descontento con servicios o atención', 'A', current_timestamp),
       (2, 'Reclamo', 'Exigencia por un derecho o servicio', 'A', current_timestamp),
       (3, 'Denuncia', 'Reporte de una irregularidad o infracción', 'A', current_timestamp),
       (4, 'Sugerencia', 'Propuesta de mejora', 'A', current_timestamp);

-- 7. Estados de Incidencia
INSERT INTO as_insidencias_estados (ie_id, ie_nombre, ie_estado, ie_fec_registro)
VALUES ('A', 'Activo / Nueva', 'A', current_timestamp),
       ('P', 'En Proceso', 'A', current_timestamp),
       ('R', 'Resuelta', 'A', current_timestamp),
       ('C', 'Cerrada / Archivada', 'A', current_timestamp);
