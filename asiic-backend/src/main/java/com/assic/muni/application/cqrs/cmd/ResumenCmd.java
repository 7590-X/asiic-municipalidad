package com.assic.muni.application.cqrs.cmd;

import jakarta.validation.constraints.Size;

public record ResumenCmd(
        @Size(max = 262144, message = "El cuerpo del resumen no puede exceder los 0.25 MB")
        String body,
        @Size(max = 20, message = "La latitud GPS no puede exceder los 20 caracteres")
        String latitudGps,
        @Size(max = 20, message = "La longitud GPS no puede exceder los 20 caracteres")
        String longitudGps
) {
}