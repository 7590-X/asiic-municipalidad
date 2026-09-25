package com.assic.muni.application.cqrs.dto;

import com.assic.muni.application.group.GrpQueja;
import jakarta.validation.constraints.NotBlank;

public record TestigoDto(
        @NotBlank(message = "El nombre del testigo es requerido", groups = GrpQueja.class)
        String nombre,
        String telefono,
        String correo
) {
}
