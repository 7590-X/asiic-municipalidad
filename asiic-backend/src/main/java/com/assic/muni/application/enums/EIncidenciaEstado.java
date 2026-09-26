package com.assic.muni.application.enums;

import lombok.Getter;

@Getter
public enum EIncidenciaEstado {
    BORRADOR("BORRADOR", "Borrador"),
    ENVIADA("ENVIADA", "Enviada"),
    ASIGNADA("ASIGNADA", "Incidencia Asignada"),
    ACEPTADA("ACEPTADA", "Incidencia Aceptada"),
    ASIGNADA_CAMPO("ASIGNADA_CAMPO", "Asignada Campo"),
    RECHAZADA("RECHAZADA", "Incidencia Rechazada"),
    APELADA("APELADA", "Incidencia Apelada"),
    APELACION_ASIGNADA("APELACION_ASIGNADA", "Apelación Asignada"),
    RECOLECCION_FIN("RECOLECCION_FIN", "Recolección Finalizada"),
    CONFIRMADA("CONFIRMADA", "Incidencia Confirmada"),
    FINALIZADA("FINALIZADA", "Incidencia Finalizada"),
    APERTURADA("APERTURADA", "Incidencia Aperturada"),
    SOLUCIONANDO("SOLUCIONANDO", "Solucionando Incidencia"),
    SOLUCIONADA("SOLUCIONADA", "Incidencia Solucionada"),
    BLOQUEADA("BLOQUEADA", "Incidencia Bloqueada");

    private final String codigo;
    private final String nombre;

    EIncidenciaEstado(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
}
