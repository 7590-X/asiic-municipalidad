package com.assic.muni.application.cqrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaDto {
    private Integer id;
    private TipoIncidenciaDto tipoIncidencia;
    private CatalogoItemDto privacidad;
    private VecinoDto vecino;
    private DomicilioDto domicilio;
    private CatalogoItemDto dependencia;
    private String direccionReferencial;
    private String nombreEmpleado;
    private Instant fechaIncidencia;
    private String descripcion;
    private String latitud;
    private String longitud;
    private String estado;
    private List<TestigoDto> testigos;
    private Instant fechaRegistro;
    private String usuarioRegistro;
    private Instant fechaModifico;
    private String usuarioModifico;
}
