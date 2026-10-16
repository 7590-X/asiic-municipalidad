package com.assic.muni.application.cqrs.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record UsuarioDto(
        String id,
        String credencial,
        String nombre,
        JsonNode roles
) {
}
