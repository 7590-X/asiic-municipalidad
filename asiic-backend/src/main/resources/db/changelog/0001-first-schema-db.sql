-- liquibase formatted sql

-- changeset rmachicm:crear-esquema-completo
CREATE TABLE as_tablas
(
    ta_id     NUMERIC(2) PRIMARY KEY,
    ta_nombre VARCHAR(45) NOT NULL UNIQUE
);

CREATE TABLE as_catalogos
(
    ca_tabla        NUMERIC(2)   NOT NULL REFERENCES as_tablas (ta_id),
    ca_id           SMALLINT     NOT NULL,
    ca_valor        VARCHAR(100) NOT NULL,
    ca_fec_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ca_id)
);

CREATE TABLE as_correos
(
    co_id           SERIAL PRIMARY KEY,
    co_correo       VARCHAR(45) NOT NULL,
    co_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    co_usr_registro VARCHAR(25) NOT NULL,
    co_fec_modifico TIMESTAMP,
    co_usr_modifico VARCHAR(25)
);

CREATE TABLE as_telefonos
(
    te_id           SERIAL PRIMARY KEY,
    te_telefono     VARCHAR(15) NOT NULL,
    te_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    te_usr_registro VARCHAR(25) NOT NULL
);

CREATE TABLE as_locaciones
(
    lo_id           SERIAL PRIMARY KEY,
    lo_pais         SMALLINT  NOT NULL,
    lo_depto        SMALLINT  NOT NULL,
    lo_muni         SMALLINT  NOT NULL,
    lo_comuna       SMALLINT  NOT NULL,
    lo_nacionalidad VARCHAR(45),
    lo_zipcode      VARCHAR(10),
    lo_fec_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lo_fec_modifico TIMESTAMP
);

CREATE TABLE as_direcciones
(
    di_id           SERIAL PRIMARY KEY,
    di_direccion    VARCHAR(100) NOT NULL,
    di_tipo         VARCHAR(1)   NOT NULL,
    di_fec_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    di_fec_modifico TIMESTAMP,
    di_locacion     INT REFERENCES as_locaciones (lo_id)
);

CREATE TABLE as_munis
(
    mu_id            SMALLSERIAL PRIMARY KEY,
    mu_estado        VARCHAR(1) NOT NULL,
    CHECK ( mu_estado IN ('A', 'I') ),
    mu_fec_fundacion DATE,
    mu_latitud       VARCHAR(20),
    mu_longitud      VARCHAR(20),
    mu_pbx           VARCHAR(12),
    mu_correo        INT        NOT NULL,
    mu_fec_registro  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mu_fec_modifico  TIMESTAMP,
    mu_direccion     INT        NOT NULL REFERENCES as_direcciones (di_id)
);

CREATE TABLE as_archivos
(
    ar_id           SERIAL PRIMARY KEY,
    ar_path         TEXT        NOT NULL,
    ar_nombre       TEXT        NOT NULL,
    ar_formato      VARCHAR(5)  NOT NULL,
    ar_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ar_version      NUMERIC(1)  NOT NULL,
    ar_hash         VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE as_personas
(
    pe_id           SERIAL PRIMARY KEY,
    pe_cui          VARCHAR(13),
    pe_nombre       VARCHAR(45) NOT NULL,
    pe_apellido     VARCHAR(45) NOT NULL,
    pe_nit          VARCHAR(12),
    pe_pasaporte    VARCHAR(20),
    pe_genero       VARCHAR(1)  NOT NULL,
    CHECK (pe_genero IN ('M', 'F')),
    pe_estado_civil SMALLINT    NOT NULL REFERENCES as_catalogos (ca_id),
    pe_tip_persona  SMALLINT    NOT NULL REFERENCES as_catalogos (ca_id)
);

CREATE TABLE as_usuarios
(
    us_id           VARCHAR(20) PRIMARY KEY,
    us_tipo         SMALLINT    NOT NULL REFERENCES as_catalogos (ca_id),
    us_estado       VARCHAR(1)  NOT NULL,
    CHECK ( us_estado IN ('A', 'I') ),
    us_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    us_ip_registro  VARCHAR(36) NOT NULL,
    us_persona      INT         NOT NULL UNIQUE REFERENCES as_personas (pe_id),
    us_usr_registro VARCHAR(20) NOT NULL,
    us_fec_modifico TIMESTAMP,
    us_ip_modifico  VARCHAR(32),
    us_usr_modifico VARCHAR(20)
);

CREATE TABLE as_tipo_insidencia
(
    ti_id           NUMERIC(2) PRIMARY KEY,
    ti_nombre       VARCHAR(30) NOT NULL UNIQUE,
    ti_descripcion  VARCHAR(75) NOT NULL,
    ti_estado       VARCHAR(1)  NOT NULL,
    CHECK ( ti_estado IN ('A', 'I') ),
    ti_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ti_fec_modifico TIMESTAMP
);

CREATE TABLE as_insidencias_estados
(
    ie_id           VARCHAR(30) PRIMARY KEY,
    ie_nombre       VARCHAR(45) NOT NULL,
    ie_estado       VARCHAR(1)  NOT NULL,
    CHECK ( ie_estado IN ('A', 'I') ),
    ie_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ie_fec_modifico TIMESTAMP
);

CREATE TABLE as_vecinos
(
    ve_id           SERIAL PRIMARY KEY,
    ve_correo       INT         NOT NULL REFERENCES as_correos (co_id),
    ve_telefono     INT         NOT NULL REFERENCES as_telefonos (te_id),
    ve_profesion    SMALLINT    NOT NULL REFERENCES as_catalogos (ca_id),
    ve_direccion    INT         NOT NULL REFERENCES as_direcciones (di_id),
    ve_estado       VARCHAR(1)  NOT NULL,
    CHECK ( ve_estado IN ('A', 'I') ),
    ve_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ve_ip_registro  VARCHAR(32) NOT NULL,
    ve_fec_modifico TIMESTAMP,
    ve_usr_modifico VARCHAR(25),
    ve_ip_modifico  VARCHAR(32)
);

CREATE TABLE as_vecinos_telefonos
(
    vt_vecino       INT         NOT NULL REFERENCES as_vecinos (ve_id),
    vt_telefono     INT         NOT NULL REFERENCES as_telefonos (te_id),
    vt_estado       VARCHAR(1)  NOT NULL,
    CHECK ( vt_estado IN ('A', 'I') ),
    vt_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vt_usr_registro VARCHAR(25) NOT NULL,
    vt_fec_modifico TIMESTAMP,
    vt_usr_modifico VARCHAR(25),
    PRIMARY KEY (vt_vecino, vt_telefono)
);

CREATE TABLE as_domicilios
(
    do_contador     VARCHAR(20) PRIMARY KEY,
    do_direccion    INT       NOT NULL UNIQUE REFERENCES as_direcciones (di_id),
    do_latitud      VARCHAR(20),
    do_longitud     VARCHAR(20),
    do_fec_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    do_fec_modifico TIMESTAMP
);

CREATE TABLE as_vecinos_domicilios
(
    vd_contador  VARCHAR(20) NOT NULL REFERENCES as_domicilios (do_contador),
    vd_vecino    INT         NOT NULL REFERENCES as_vecinos (ve_id),
    vd_condicion SMALLINT    NOT NULL REFERENCES as_catalogos (ca_id),
    vd_estado    VARCHAR(1)  NOT NULL,
    CHECK ( vd_estado IN ('A', 'I') ),
    PRIMARY KEY (vd_contador, vd_vecino)
);

CREATE TABLE as_insidencias
(
    in_id              SERIAL PRIMARY KEY,
    in_privacidad      SMALLINT     NOT NULL REFERENCES as_catalogos (ca_id),
    in_vecino          INT          NOT NULL REFERENCES as_vecinos (ve_id),
    in_contador        VARCHAR(20)  NOT NULL REFERENCES as_domicilios (do_contador),
    in_direccion       VARCHAR(100) NOT NULL,
    in_unidad          SMALLINT REFERENCES as_catalogos (ca_id),
    in_empleado        VARCHAR(100),
    in_fec_insidencia  TIMESTAMP    NOT NULL,
    in_lugar           SMALLINT REFERENCES as_catalogos (ca_id),
    in_comentario      TEXT         NOT NULL,
    in_latitud         VARCHAR(20),
    in_longitud        VARCHAR(20),
    in_estado          VARCHAR(30)  NOT NULL REFERENCES as_insidencias_estados (ie_id),
    in_tipo_servicio   SMALLINT     NOT NULL REFERENCES as_catalogos (ca_id),
    in_tipo_insidencia NUMERIC(2)   NOT NULL REFERENCES as_tipo_insidencia (ti_id),
    in_tipo_denuncia   SMALLINT REFERENCES as_catalogos (ca_id),
    in_area            SMALLINT REFERENCES as_catalogos (ca_id),
    in_propuesta       TEXT         NOT NULL,
    in_fec_registro    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    in_usr_registro    VARCHAR(25)  NOT NULL,
    in_fec_modifico    TIMESTAMP,
    in_usr_modifico    VARCHAR(25),
    in_analista        VARCHAR(20)  NULL REFERENCES as_usuarios (us_id)
);

CREATE TABLE as_denunciados
(
    de_id         SERIAL PRIMARY KEY,
    de_nombre     VARCHAR(75) NOT NULL,
    de_tipo       SMALLINT    NOT NULL REFERENCES as_catalogos (ca_id),
    de_estado     VARCHAR(1)  NOT NULL,
    CHECK ( de_estado IN ('A', 'I') ),
    de_insidencia INT         NOT NULL REFERENCES as_insidencias (in_id)
);

CREATE TABLE as_insidencias_archivos
(
    ia_insidencia   INT         NOT NULL REFERENCES as_insidencias (in_id),
    ai_archivo      INT         NOT NULL PRIMARY KEY REFERENCES as_archivos (ar_id),
    ai_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ai_usr_registro VARCHAR(25) NOT NULL,
    ai_fec_modifico TIMESTAMP,
    ai_usr_modifico VARCHAR(25),
    ai_estado       VARCHAR(1)  NOT NULL,
    CHECK ( ai_estado IN ('A', 'I') )
);

CREATE TABLE as_investigaciones_campo
(
    ic_secuancial   NUMERIC(2)  NOT NULL,
    ic_insidencia   INT         NOT NULL REFERENCES as_insidencias (in_id),
    ic_agente       VARCHAR(20) NOT NULL REFERENCES as_usuarios (us_id),
    ic_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ic_usr_registro VARCHAR(20) NOT NULL,
    ic_fec_modifico TIMESTAMP,
    ic_usr_modifico VARCHAR(20),
    ic_estado       VARCHAR(1)  NOT NULL,
    CHECK ( ic_estado IN ('P', 'C') ),
    PRIMARY KEY (ic_secuancial, ic_insidencia)
);

CREATE TABLE as_documentos_investigacion
(
    di_secuencia    NUMERIC(2)  NOT NULL,
    di_insidencia   INT         NOT NULL,
    di_archivo      INT         NOT NULL PRIMARY KEY,
    di_fec_registro TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    di_usr_registro VARCHAR(20) NOT NULL,
    di_fec_modifico TIMESTAMP,
    di_usr_modifico VARCHAR(20),
    di_estado       VARCHAR(1)  NOT NULL,
    CHECK ( di_estado IN ('A', 'I') ),
    FOREIGN KEY (di_secuencia, di_insidencia)
        REFERENCES as_investigaciones_campo (ic_secuancial, ic_insidencia),
    FOREIGN KEY (di_archivo)
        REFERENCES as_archivos (ar_id)
);

CREATE TABLE as_testigos
(
    te_persona    INT NOT NULL,
    te_insidencia INT NOT NULL,
    te_correo     INT,
    te_telefono   INT NOT NULL,
    PRIMARY KEY (te_persona, te_insidencia),
    FOREIGN KEY (te_persona)
        REFERENCES as_personas (pe_id),
    FOREIGN KEY (te_insidencia)
        REFERENCES as_insidencias (in_id),
    FOREIGN KEY (te_correo)
        REFERENCES as_correos (co_id),
    FOREIGN KEY (te_telefono)
        REFERENCES as_telefonos (te_id)
);

-- rollback DROP TABLE as_testigos CASCADE;
-- rollback DROP TABLE as_documentos_investigacion CASCADE;
-- rollback DROP TABLE as_investigaciones_campo CASCADE;
-- rollback DROP TABLE as_insidencias_archivos CASCADE;
-- rollback DROP TABLE as_denunciados CASCADE;
-- rollback DROP TABLE as_insidencias CASCADE;
-- rollback DROP TABLE as_vecinos_domicilios CASCADE;
-- rollback DROP TABLE as_domicilios CASCADE;
-- rollback DROP TABLE as_vecinos_telefonos CASCADE;
-- rollback DROP TABLE as_vecinos CASCADE;
-- rollback DROP TABLE as_insidencias_estados CASCADE;
-- rollback DROP TABLE as_tipo_insidencia CASCADE;
-- rollback DROP TABLE as_usuarios CASCADE;
-- rollback DROP TABLE as_personas CASCADE;
-- rollback DROP TABLE as_archivos CASCADE;
-- rollback DROP TABLE as_munis CASCADE;
-- rollback DROP TABLE as_direcciones CASCADE;
-- rollback DROP TABLE as_locaciones CASCADE;
-- rollback DROP TABLE as_telefonos CASCADE;
-- rollback DROP TABLE as_correos CASCADE;
-- rollback DROP TABLE as_catalogos CASCADE;
-- rollback DROP TABLE as_tablas CASCADE;