package com.assic.muni.application.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetIncidenciaByIdQuery {
    private String incidenciaId;
    private String userId;
}
