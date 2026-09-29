package com.assic.muni.application.cqrs.dto;

import com.assic.muni.application.group.GrpDenuncia;
import jakarta.validation.constraints.NotBlank;

public record DenunciadoDto(
        String nitCui,
        String carnet,

        @NotBlank(message = "El nombre del denunciado es requerido", groups = GrpDenuncia.class)
        String nombre,
        String telefono,
        String correo,
        String puesto,
        String noPlaca
) {
}
