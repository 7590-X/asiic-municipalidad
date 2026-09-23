package com.assic.muni.application.cqrs.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncidenciaDetalleDto {
    private String id;
    private String tipoIncidencia; // QUEJA, RECLAMO, DENUNCIA, SUGERENCIA
    private String privacidad;
    private String estado;
    
    // Datos de la incidencia que apliquen
    private Short dependenciaId;
    private String empleadoId;
    private String fechaIncidencia;
    private String lugar;
    private String descripcion;
    private String testigoNombre;
    private String testigoTelefono;
    private String testigoCorreo;

    private Short tipoServicioId;
    private String ubicacionGps;
    private String direccion;
    private String noContador;

    private Short tipoDenunciaId;
    private String denunciados;
    private String fechaHoraHechos;
    private String relato;

    private Short areaId;
    private String descripcionActual;
    private String propuestaMejora;
}
