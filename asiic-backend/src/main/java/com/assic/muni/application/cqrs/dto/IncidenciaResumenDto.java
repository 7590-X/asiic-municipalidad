package com.assic.muni.application.cqrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaResumenDto {
    private String id;
    private String tipo;
    private String asunto;
    private String dependencia;
    private String fecha;
    private String estado;
}
