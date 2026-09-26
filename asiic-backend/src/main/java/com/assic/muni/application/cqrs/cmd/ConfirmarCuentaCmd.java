package com.assic.muni.application.cqrs.cmd;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ConfirmarCuentaCmd(
        @NotNull(message = "El token es requerido")
        String token,

        @NotNull(message = "Contraseña es requerida")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$",
                message = "Contraseña insegura, debe de tener como mínimo una letra mayúscula y un número"
        )
        String password
){}
