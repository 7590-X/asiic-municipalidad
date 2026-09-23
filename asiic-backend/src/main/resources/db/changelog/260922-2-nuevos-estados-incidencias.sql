-- liquibase formatted sql

-- changeset asiic:nuevos-estados-incidencias

-- 1. Insertar nuevos estados
INSERT INTO as_insidencias_estados (ie_id, ie_nombre, ie_estado, ie_fec_registro) VALUES
('BORRADOR', 'Borrador', 'A', current_timestamp),
('ENVIADA', 'Enviada', 'A', current_timestamp),
('ASIGNADA', 'Incidencia Asignada', 'A', current_timestamp),
('ACEPTADA', 'Incidencia Aceptada', 'A', current_timestamp),
('ASIGNADA_CAMPO', 'Asignada Campo', 'A', current_timestamp),
('RECHAZADA', 'Incidencia Rechazada', 'A', current_timestamp),
('APELADA', 'Incidencia Apelada', 'A', current_timestamp),
('APELACION_ASIGNADA', 'Apelación Asignada', 'A', current_timestamp),
('RECOLECCION_FIN', 'Recolección Finalizada', 'A', current_timestamp),
('CONFIRMADA', 'Incidencia Confirmada', 'A', current_timestamp),
('FINALIZADA', 'Incidencia Finalizada', 'A', current_timestamp),
('APERTURADA', 'Incidencia Aperturada', 'A', current_timestamp),
('SOLUCIONANDO', 'Solucionando Incidencia', 'A', current_timestamp),
('SOLUCIONADA', 'Incidencia Solucionada', 'A', current_timestamp),
('BLOQUEADA', 'Incidencia Bloqueada', 'A', current_timestamp);

-- 2. Migrar incidencias existentes al estado ENVIADA
UPDATE as_insidencias SET in_estado = 'ENVIADA' WHERE in_estado IN ('A', 'P', 'R', 'C');

-- 3. Borrar estados viejos
DELETE FROM as_insidencias_estados WHERE ie_id IN ('A', 'P', 'R', 'C');
