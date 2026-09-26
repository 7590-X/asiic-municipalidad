package com.assic.muni.application.cqrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VecinoMeDto {
    private Integer id;
    private String dpi;
    private String nombresApellidos;
    private String correo;
    private String telefono;
    private String direccion;
}
