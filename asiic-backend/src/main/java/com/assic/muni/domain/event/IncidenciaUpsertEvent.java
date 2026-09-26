package com.assic.muni.domain.event;

import java.time.Instant;

public record IncidenciaUpsertEvent(
        int id,
        Instant fechaRegistro,
        String estado,
        String vecino,
        String email
) {
}
