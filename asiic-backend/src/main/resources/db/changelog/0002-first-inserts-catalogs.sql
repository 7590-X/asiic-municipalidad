-- liquibase formatted sql

-- changeset nildev:agregar-catalogos-desarrollo

INSERT INTO as_tablas (ta_id, ta_nombre)
VALUES (1, 'as_estado_civil'),
       (2, 'as_condicion_vecino');


INSERT INTO as_catalogos (ca_tabla, ca_id, ca_valor)
VALUES (1, 1, 'Soltero(a)'),
       (1, 2, 'Casado(a)'),
       (1, 3, 'Divorciado(a)'),
       (1, 4, 'Viudo(a)'),
       (2, 5, 'Propietario'),
       (2, 6, 'Inquilino'),
       (2, 7, 'Usufructuario');

INSERT INTO as_locaciones (lo_pais, lo_depto, lo_muni, lo_comuna, lo_nacionalidad, lo_zipcode, lo_fec_registro,
                           lo_descripcion)
VALUES (1, 0, 0, 0, 'Guatemalteco', null, current_timestamp, 'Guatemala'),
       (1, 1, 0, 0, null, null, current_timestamp, 'Guatemala'),
       (1, 1, 1, 0, null, null, current_timestamp, 'Ciudad de Guatemala'),
       (1, 1, 1, 1, null, null, current_timestamp, 'Zona 1'),
       (1, 1, 1, 2, null, null, current_timestamp, 'Zona 2'),
       (1, 1, 1, 3, null, null, current_timestamp, 'Zona 3'),
       (1, 1, 1, 4, null, null, current_timestamp, 'Zona 4'),
       (1, 1, 1, 5, null, null, current_timestamp, 'Zona 5'),
       (1, 1, 1, 6, null, null, current_timestamp, 'Zona 6'),
       (1, 1, 1, 7, null, null, current_timestamp, 'Zona 7'),
       (1, 1, 1, 8, null, null, current_timestamp, 'Zona 8'),
       (1, 1, 1, 9, null, null, current_timestamp, 'Zona 9'),
       (1, 1, 1, 10, null, null, current_timestamp, 'Zona 10'),
       (1, 1, 1, 11, null, null, current_timestamp, 'Zona 11'),
       (1, 1, 1, 12, null, null, current_timestamp, 'Zona 12'),
       (1, 1, 1, 13, null, null, current_timestamp, 'Zona 13'),
       (1, 1, 1, 14, null, null, current_timestamp, 'Zona 14'),
       (1, 1, 1, 15, null, null, current_timestamp, 'Zona 15'),
       (1, 1, 1, 16, null, null, current_timestamp, 'Zona 16'),
       (1, 1, 1, 17, null, null, current_timestamp, 'Zona 17'),
       (1, 1, 1, 18, null, null, current_timestamp, 'Zona 18'),
       (1, 1, 1, 19, null, null, current_timestamp, 'Zona 19'),
       (1, 1, 1, 20, null, null, current_timestamp, 'Zona 20'),
       (1, 1, 1, 21, null, null, current_timestamp, 'Zona 21'),
       (1, 1, 1, 22, null, null, current_timestamp, 'Zona 22'),
       (1, 1, 1, 23, null, null, current_timestamp, 'Zona 23'),
       (1, 1, 1, 24, null, null, current_timestamp, 'Zona 24');