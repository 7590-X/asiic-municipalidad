-- liquibase formatted sql
-- changeset nildev:agregar-municipio-mixco
INSERT INTO as_locaciones (lo_pais, lo_depto, lo_muni, lo_comuna, lo_nacionalidad, lo_zipcode, lo_fec_registro, lo_descripcion)
VALUES

    (1, 1, 2, 0, null, null, current_timestamp, 'Mixco'),
    (1, 1, 2, 1, null, null, current_timestamp, 'Zona 1'),
    (1, 1, 2, 4, null, null, current_timestamp, 'Zona 4 (El Naranjo)'),
    (1, 1, 2, 8, null, null, current_timestamp, 'Zona 8 (San Cristobal)');