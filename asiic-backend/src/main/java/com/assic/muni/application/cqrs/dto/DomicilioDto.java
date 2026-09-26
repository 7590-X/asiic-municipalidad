package com.assic.muni.application.cqrs.dto;

public record DomicilioDto(
        String contador,
        String latitud,
        String longitud,
        DireccionDto direccion
) {
}
