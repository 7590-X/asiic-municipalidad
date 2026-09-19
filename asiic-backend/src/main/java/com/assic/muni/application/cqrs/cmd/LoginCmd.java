package com.assic.muni.application.cqrs.cmd;

import jakarta.validation.constraints.NotBlank;

public record LoginCmd(
        @NotBlank(message = "El nombre de usuario o correo no puede estar vacío")
        String username,

        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
}
