package com.assic.muni.application.cqrs.dto;

import java.time.LocalDate;

public record MuniDto(
        Short id,
        LocalDate fechaFundacion,
        String latitud,
        String longitud,
        String pbx,
        String correo,
        String direccion,
        String nombre,
        String nit,
        String municipio
) {
}
