package com.assic.muni.application.cqrs.dto;

public record VecinoDto(
        Integer id,
        String cui,
        String nombreCompleto,
        String correo,
        String telefono
) {
}
