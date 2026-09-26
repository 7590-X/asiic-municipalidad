-- liquibase formatted sql
-- changeset nildev:catalogos-registro-vecino
INSERT INTO as_tablas (ta_id, ta_nombre)
VALUES (3, 'as_tipo_persona'),
       (4, 'as_profesion');

CALL sp_agregar_catalogo(3, 'Individual');
CALL sp_agregar_catalogo(3, 'Juridica');
CALL sp_agregar_catalogo(4, 'Sin profesión');
CALL sp_agregar_catalogo(4, 'Estudiante');
CALL sp_agregar_catalogo(4, 'Comerciante');
CALL sp_agregar_catalogo(4, 'Profesional');
CALL sp_agregar_catalogo(4, 'Jubilado');
