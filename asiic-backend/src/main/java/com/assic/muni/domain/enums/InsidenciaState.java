package com.assic.muni.domain.enums;

import lombok.Getter;

/**
 * Listado de estados de una solicitud de insidencia
 */
public enum InsidenciaState {
    BORRADOR("Borrador"),
    ENVIADA("Enviada"),
    ASIGNADA("Incidencia Asignada"),
    ACEPTADA("Incidencia Aceptada"),
    ASIGNADA_CAMPO("Asignada Campo"),
    RECHAZADA("Incidencia Rechazada"),
    APELADA("Incidencia Apelada"),
    APELACION_ASIGNADA("Apelación Asignada"),
    RECOLECCION_FIN("Recolección Finalizada"),
    CONFIRMADA("Incidencia Confirmada"),
    FINALIZADA("Incidencia Finalizada"),
    APERTURADA("Incidencia Aperturada"),
    SOLUCIONANDO("Solucionando Incidencia"),
    SOLUCIONADA("Incidencia Solucionada"),
    BLOQUEADA("Incidencia Bloqueada");

    @Getter
    private final String descripcion;

    InsidenciaState(String descripcion) {
        this.descripcion = descripcion;
    }
}
