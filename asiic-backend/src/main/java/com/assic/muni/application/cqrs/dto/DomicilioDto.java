package com.assic.muni.application.cqrs.dto;

public record DomicilioDto(
        String contador,
        String direccion,
        String latitud,
        String longitud
) {
}
