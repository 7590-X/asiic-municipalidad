package com.assic.muni.application.cqrs.cmd;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MuniPayloadCmd(

        Short municipalidadId,

        @NotNull(message = "El código de municipio es obligatorio")
        Integer municipioId,

        @NotBlank(message = "El nombre de la municipalidad es obligatorio")
        String nombre,

        @NotBlank(message = "El NIT de la municipalidad es obligatorio")
        @Size(min = 4, max = 13, message = "El NIT debe de tener entre 4 y 13 caracteres")
        @Pattern(regexp = "^\\d*[A-Z]?$", message = "Formato de NIT es invalido")
        String nit,

        @Past(message = "La fecha de fundación debe ser anterior a la fecha actual")
        LocalDate fechaFundacion,

        @NotBlank(message = "La dirección fiscal es obligatoria")
        @Size(max = 100, message = "La dirección fiscal debe tener un máximo de 100 caracteres")
        String direccionFiscal,

        @NotBlank(message = "La latitud GPS es obligatoria")
        @Size(max = 20, message = "La latitud GPS debe tener un máximo de 10 caracteres")
        String latitudGps,

        @NotBlank(message = "La longitud GPS es obligatoria")
        @Size(max = 20, message = "La longitud GPS debe tener un máximo de 10 caracteres")
        String longitudGps,

        @Size(max = 12, message = "El número de teléfono debe tener un máximo de 12 caracteres")
        String pbx,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico es invalido")
        String correo
) {
}
