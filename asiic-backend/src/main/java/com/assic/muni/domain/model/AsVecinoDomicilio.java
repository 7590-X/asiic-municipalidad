package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "as_vecinos_domicilios")
public class AsVecinoDomicilio {
    @EmbeddedId
    private AsVecinoDomicilioId id;

    @MapsId("vdContador")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vd_contador", nullable = false)
    private AsDomicilio vdContador;

    @MapsId("vdVecino")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vd_vecino", nullable = false)
    private AsVecino vdVecino;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vd_condicion", nullable = false)
    private AsCatalogo vdCondicion;

    @Size(max = 1)
    @NotNull
    @Column(name = "vd_estado", nullable = false, length = 1)
    private String vdEstado;


}