package com.assic.muni.application.cqrs.cmd;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.assic.muni.application.cqrs.dto.TestigoDto;
import com.assic.muni.application.group.GrpQueja;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class IncidenciaPayloadCmd {

    /**
     * Código de incidencia, requerido cuando se actualiza la incidencia
     */
    private Integer incidenciaId;

    /**
     * QUEJA, RECLAMO, DENUNCIA, SUGERENCIA
     */
    @NotNull(message = "El código de insidencia es requerido", groups = {GrpQueja.class})
    private Short tipoIncidencia;

    /**
     * Debe de almacenar el pseudonimo del tipo de privacidad
     */
    @NotBlank(message = "El pseudonimo de privacidad es requerido", groups = {GrpQueja.class})
    private String privacidad;

    /**
     * Número de contador
     */
    @NotBlank(message = "El número de contador del domicilio es requerido", groups = {GrpQueja.class})
    private String contador;

    /**
     * Estructura de queja
     */
    @Valid
    private DetalleQuejaDto detalleQueja;

    /**
     * Estructura de reclamo
     */
    @Valid
    private DetalleReclamoDto detalleReclamo;

    /**
     * Estructura de denuncia
     */
    @Valid
    private DetalleDenunciaDto detalleDenuncia;

    /**
     * Estructura de sugerencia
     */
    @Valid
    private DetalleSugerenciaDto detalleSugerencia;

    @Data
    public static class DetalleQuejaDto {
        @NotNull(message = "El ID de dependencia es requerido", groups = GrpQueja.class)
        private Short dependenciaId; // En la tabla es "unidad"

        private String nombreEmpleado;

        @PastOrPresent(message = "La fecha de la incidencia debe ser anterior o igual a la actual", groups = GrpQueja.class)
        private Instant fechaIncidencia;

        @NotBlank(message = "El lugar de la queja es requerido", groups = GrpQueja.class)
        private String direccionReferencial;

        @NotBlank(message = "La descripción de la queja es requerida", groups = GrpQueja.class)
        private String descripcion;

        private List<@Valid TestigoDto> testigo;

        private String latitudGps;

        private String longitudGps;
    }

    @Data
    public static class DetalleReclamoDto {
        private Short tipoServicioId;
        private String latitudGps;
        private String longitudGps;
        private String direccion;
        private String noContador;
        private String descripcion;
    }

    @Data
    public static class DetalleDenunciaDto {
        private Short tipoDenunciaId;
        private DenunciadoDto[] denunciados; // generar una estructura y almacenar como JSON en db
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

    /**
     * Estructura para denunciados para solicitudes de denuncias ciudadanas
     */
    @Data
    public static class DenunciadoDto {
        private String nombre;
        private String cui;
        private String direccion;
        private String telefono;
        private String correo;
    }
}
