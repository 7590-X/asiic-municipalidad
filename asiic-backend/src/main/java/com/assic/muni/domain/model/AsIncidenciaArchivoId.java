package com.assic.muni.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AsIncidenciaArchivoId {

    @Column(name = "ai_archivo", nullable = false)
    private Integer aiArchivo;

    @Column(name = "ia_insidencia", nullable = false)
    private Integer aiIncidencia;

}
