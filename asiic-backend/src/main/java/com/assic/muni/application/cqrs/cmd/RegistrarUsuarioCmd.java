package com.assic.muni.application.cqrs.cmd;

import java.util.List;

public record RegistrarUsuarioCmd(
        String nombres,
        String apellidos,
        String correo,
        List<String> roles,
        List<Short> municipalidades
) {
}
