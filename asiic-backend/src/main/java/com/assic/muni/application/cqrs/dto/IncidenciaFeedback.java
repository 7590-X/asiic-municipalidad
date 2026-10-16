package com.assic.muni.application.cqrs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncidenciaFeedback(
        @Size(max = 75, message = "El título no puede tener más de 75 caracteres")
        @NotBlank(message = "El título no puede estar vacío")
        String titulo,

        @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
        @NotBlank(message = "La descripción no puede estar vacía")
        String descripcion,

        @Size(max = 255, message = "La recomendación no puede tener más de 255 caracteres")
        String recomendacion
) {
}
