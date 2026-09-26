package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@Table(name = "as_vecinos_domicilios")
@AllArgsConstructor
@NoArgsConstructor
public class AsVecinoDomicilio {

    @EmbeddedId
    private AsVecinoDomicilioId id;

    @MapsId("vdContador")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vd_contador", nullable = false)
    private AsDomicilio vdDomicilio;

    @MapsId("vdVecino")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vd_vecino", nullable = false)
    private AsVecino vdVecino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vd_condicion")
    private AsCatalogo vdCondicion;

    @Size(max = 1)
    @NotNull
    @Column(name = "vd_estado", nullable = false, length = 1)
    private String vdEstado;
}