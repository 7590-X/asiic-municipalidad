package com.assic.muni.application.port.out.dto;

public record KCUsuario(
        String username,
        String email,
        String firstName,
        String lastName,
        boolean emailVerified,
        boolean accountEnabled
) {
}
