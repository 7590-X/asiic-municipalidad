-- liquibase formatted sql

-- changeset rmachicm:alteracion-pk-incidencias-archivos splitStatements:false endDelimiter:$$
DO
$$
    DECLARE
        v_pk TEXT;
    BEGIN
        -- Obtener la clave primaria actual
        SELECT conname INTO v_pk
        FROM pg_constraint
        WHERE conrelid = 'as_insidencias_archivos'::regclass
          AND contype = 'p';

        -- Eliminarla si existe
        IF v_pk IS NOT NULL THEN
            EXECUTE format('ALTER TABLE as_insidencias_archivos DROP CONSTRAINT %I CASCADE', v_pk);
            RAISE NOTICE 'Restricción % eliminada correctamente.', v_pk;
        ELSE
            RAISE NOTICE 'No se encontró ninguna clave primaria para la tabla.';
        END IF;

        -- Crear la nueva clave primaria compuesta
        -- NOTA: Verifica si tus columnas son ai_insidencia o ia_insidencia
        ALTER TABLE as_insidencias_archivos
            ADD CONSTRAINT pk_as_insidencias_archivos PRIMARY KEY (ia_insidencia, ai_archivo);
    END
$$;