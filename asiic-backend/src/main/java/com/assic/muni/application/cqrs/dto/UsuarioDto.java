package com.assic.muni.application.cqrs.dto;

import java.util.List;

public record UsuarioDto(
        String id,
        String nombre,
        List<String> roles,
        List<Short> municipalidades,
        String estado
) {
}
