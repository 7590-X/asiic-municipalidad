package com.assic.muni.application.cqrs.dto;

public record DireccionDto(
        int idDireccion,
        String descripDireccion,
        String pais,
        String departamento,
        String municipio,
        String comuna
) {
}
