package com.assic.muni.application.cqrs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IncidenciaPayloadDto {
    private String tipoIncidencia; // QUEJA, RECLAMO, DENUNCIA, SUGERENCIA
    private String privacidad;
    private Boolean esBorrador;
    
    private DetalleQuejaDto detalleQueja;
    private DetalleReclamoDto detalleReclamo;
    private DetalleDenunciaDto detalleDenuncia;
    private DetalleSugerenciaDto detalleSugerencia;

    @Data
    public static class DetalleQuejaDto {
        private Short dependenciaId;
        private String empleadoId;
        private LocalDateTime fechaIncidencia;
        private String lugar;
        private String descripcion;
        private TestigoDto testigo;
    }

    @Data
    public static class DetalleReclamoDto {
        private Short tipoServicioId;
        private String ubicacionGps;
        private String direccion;
        private String noContador;
        private String descripcion;
    }

    @Data
    public static class DetalleDenunciaDto {
        private Short tipoDenunciaId;
        private String denunciados;
        private LocalDateTime fechaHoraHechos;
        private String direccion;
        private String relato;
    }

    @Data
    public static class DetalleSugerenciaDto {
        private Short areaId;
        private String descripcionActual;
        private String propuestaMejora;
    }

    @Data
    public static class TestigoDto {
        private String nombre;
        private String telefono;
        private String correo;
    }
}
