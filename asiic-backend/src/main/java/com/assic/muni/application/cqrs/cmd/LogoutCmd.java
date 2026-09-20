package com.assic.muni.application.cqrs.cmd;

import jakarta.validation.constraints.NotBlank;

public record LogoutCmd(
        @NotBlank(message = "El refresh token no puede estar vacío")
        String refreshToken
) {
}
