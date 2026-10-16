-- liquibase formatted sql

-- changeset rmachicm:modificar-SP-asignar_agente splitStatements:false
CREATE OR REPLACE PROCEDURE sp_asignacion_agente()
    LANGUAGE plpgsql
AS
$$
DECLARE
    c_incidencias CURSOR FOR
        SELECT in_id AS id, in_contador AS contador
        FROM as_incidencias
        WHERE in_estado IN ('ACEPTADA')
        ORDER BY in_fec_modifico ASC
            FOR UPDATE;
    w_record           RECORD;
    w_muni_id          INT;
    w_municipalidad_id INT;
    w_agente           VARCHAR(36);
    w_locacion_id      INT;

    -- Variables para captura de errores
    v_err_msg          TEXT;
    v_err_state        TEXT;
    v_err_context      TEXT;
BEGIN
    OPEN c_incidencias;
    -- 12,2100022
    LOOP
        FETCH c_incidencias INTO w_record;
        EXIT WHEN NOT FOUND;

        -- Sub-bloque protegido por cada incidencia
        BEGIN
            -- Obtener el subcodigo de municipio
            SELECT lo.lo_muni
            INTO STRICT w_muni_id
            FROM as_domicilios d
                     INNER JOIN as_direcciones dir ON dir.di_id = d.do_direccion
                     INNER JOIN as_locaciones lo ON lo.lo_id = dir.di_locacion
            WHERE d.do_contador = w_record.contador;

            -- Obtener el ID del municipio
            select lo_id
            into w_locacion_id
            from as_locaciones
            where lo_muni = w_muni_id
              and lo_comuna = 0;

            -- Obtener municipalidad encargada
            SELECT m.mu_id
            INTO STRICT w_municipalidad_id
            FROM as_munis m
                     INNER JOIN as_direcciones dir ON dir.di_id = m.mu_direccion
            WHERE dir.di_locacion = w_locacion_id;

            -- Obtener el agente campo con menor carga asignado a esa municipalidad
            SELECT mu.mu_usuario
            INTO w_agente
            FROM as_munis_usuarios mu
                     INNER JOIN as_usuarios u
                                ON u.us_id = mu.mu_usuario
                                    AND u.us_estado = 'A'
                     LEFT JOIN as_incidencias i
                               ON i.in_agente = mu.mu_usuario
            WHERE mu.mu_estado = 'A'
              AND mu.mu_muni = w_municipalidad_id -- Municipalidad donde se envió la solicitud
              AND mu.mu_tipo_usr = 17             -- sys_agente
            GROUP BY mu.mu_usuario
            ORDER BY COUNT(i.in_id) ASC,
                     MIN(mu.mu_fec_registro) ASC
            LIMIT 1;

            IF w_agente IS NULL THEN
                RAISE EXCEPTION 'No hay agentes de campo activos disponibles para la municipalidad ID %', w_muni_id;
            END IF;

            -- Actualizar la incidencia apuntada por el cursor
            UPDATE as_incidencias
            SET in_agente       = w_agente,
                in_estado       = 'ASIGNADA_CAMPO',
                in_fec_modifico = CURRENT_TIMESTAMP,
                in_usr_modifico = USER
            WHERE CURRENT OF c_incidencias;

            RAISE NOTICE '[OK] Incidencia ID % asignada al agente de campo % (Muni: %)',
                w_record.id, w_agente, w_municipalidad_id;

        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                GET STACKED DIAGNOSTICS
                    v_err_msg = MESSAGE_TEXT,
                    v_err_state = RETURNED_SQLSTATE;
                RAISE WARNING '[DATOS FALTANTES] Incidencia ID % (Contador %): No se encontró domicilio o municipalidad asociada. Estado: %',
                    w_record.id, w_record.contador, v_err_state;

            WHEN TOO_MANY_ROWS THEN
                GET STACKED DIAGNOSTICS
                    v_err_msg = MESSAGE_TEXT,
                    v_err_state = RETURNED_SQLSTATE;
                RAISE WARNING '[DUPLICADOS] Incidencia ID %: Múltiples domicilios/municipios encontrados para el contador %. Estado: %',
                    w_record.id, w_record.contador, v_err_state;

            WHEN OTHERS THEN
                GET STACKED DIAGNOSTICS
                    v_err_msg = MESSAGE_TEXT,
                    v_err_state = RETURNED_SQLSTATE,
                    v_err_context = PG_EXCEPTION_CONTEXT;
                RAISE WARNING '[FALLO] Incidencia ID % omitida por error: % (Código: %). Contexto: %',
                    w_record.id, v_err_msg, v_err_state, v_err_context;
        END;

    END LOOP;

    CLOSE c_incidencias;
EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS
            v_err_msg = MESSAGE_TEXT,
            v_err_state = RETURNED_SQLSTATE,
            v_err_context = PG_EXCEPTION_CONTEXT;
        ROLLBACK;
        RAISE EXCEPTION 'Fallo crítico global en sp_asignacion_analista: % [SQLSTATE %] - Pila: %',
            v_err_msg, v_err_state, v_err_context;
END;
$$;