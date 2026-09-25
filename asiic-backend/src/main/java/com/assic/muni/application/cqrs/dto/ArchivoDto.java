package com.assic.muni.application.cqrs.dto;

import java.time.Instant;

public record ArchivoDto(int id, String nombre, String formato, Instant fechaRegistro) {
}
