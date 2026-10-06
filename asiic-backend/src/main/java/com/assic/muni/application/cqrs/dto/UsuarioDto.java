package com.assic.muni.application.cqrs.dto;

public record UsuarioDto(
        String id,
        String nombre,
        String rol,
        String estado
) {
}
