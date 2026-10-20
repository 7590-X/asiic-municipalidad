-- Tabla de auditoría
CREATE TABLE as_incidencias_auditoria
(
    aud_id            BIGSERIAL PRIMARY KEY,
    in_id             INTEGER                                            NOT NULL,
    aud_operacion     VARCHAR(10)                                        NOT NULL, -- 'INSERT', 'UPDATE', 'DELETE'
    aud_fecha         TIMESTAMP WITH TIME ZONE DEFAULT CLOCK_TIMESTAMP() NOT NULL,
    aud_usuario_db    VARCHAR(64)              DEFAULT CURRENT_USER      NOT NULL,
    aud_usuario_app   VARCHAR(36),
    aud_datos_previos JSONB,
    aud_datos_nuevos  JSONB
);

-- Índices recomendados para consultas rápidas por incidencia o fecha
CREATE INDEX idx_aud_incidencias_in_id ON as_incidencias_auditoria (in_id);
CREATE INDEX idx_aud_incidencias_fecha ON as_incidencias_auditoria (aud_fecha DESC);

ALTER TABLE as_incidencias_auditoria
    OWNER TO usr_asiic;

-- 2. Función del Trigger
CREATE OR REPLACE FUNCTION fn_aud_as_incidencias()
    RETURNS TRIGGER AS
$$
DECLARE
    v_usuario_app VARCHAR(36);
BEGIN
    -- Captura el usuario aplicativo: primero evalúa variables de sesión (si existen),
    -- luego los campos de usuario de la propia tabla, o NULL
    v_usuario_app := COALESCE(
            NULLIF(CURRENT_SETTING('app.current_user', true), ''),
            CASE
                WHEN TG_OP = 'INSERT' THEN NEW.in_usr_registro
                WHEN TG_OP = 'UPDATE' THEN COALESCE(NEW.in_usr_modifico, NEW.in_usr_registro)
                WHEN TG_OP = 'DELETE' THEN COALESCE(OLD.in_usr_modifico, OLD.in_usr_registro)
                END
                     );

    IF (TG_OP = 'INSERT') THEN
        INSERT INTO as_incidencias_auditoria (in_id,
                                              aud_operacion,
                                              aud_usuario_app,
                                              aud_datos_previos,
                                              aud_datos_nuevos)
        VALUES (NEW.in_id,
                TG_OP,
                v_usuario_app,
                NULL,
                TO_JSONB(NEW));
        RETURN NEW;

    ELSIF (TG_OP = 'UPDATE') THEN
        -- Opcional: solo auditar si hubo cambios reales en los datos
        IF TO_JSONB(NEW) IS DISTINCT FROM TO_JSONB(OLD) THEN
            INSERT INTO as_incidencias_auditoria (in_id,
                                                  aud_operacion,
                                                  aud_usuario_app,
                                                  aud_datos_previos,
                                                  aud_datos_nuevos)
            VALUES (NEW.in_id,
                    TG_OP,
                    v_usuario_app,
                    TO_JSONB(OLD),
                    TO_JSONB(NEW));
        END IF;
        RETURN NEW;

    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO as_incidencias_auditoria (in_id,
                                              aud_operacion,
                                              aud_usuario_app,
                                              aud_datos_previos,
                                              aud_datos_nuevos)
        VALUES (OLD.in_id,
                TG_OP,
                v_usuario_app,
                TO_JSONB(OLD),
                NULL);
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Definición del Trigger
DROP TRIGGER IF EXISTS trg_aud_as_incidencias ON as_incidencias;

CREATE TRIGGER trg_aud_as_incidencias
    AFTER INSERT OR UPDATE OR DELETE
    ON as_incidencias
    FOR EACH ROW
EXECUTE FUNCTION fn_aud_as_incidencias();


select * from as_incidencias;

select * from as_incidencias_auditoria