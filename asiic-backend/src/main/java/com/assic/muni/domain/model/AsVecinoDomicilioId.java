package com.assic.muni.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class AsVecinoDomicilioId implements Serializable {
    @Serial
    private static final long serialVersionUID = -6780642524501862398L;
    @Size(max = 20)
    @NotNull
    @Column(name = "vd_contador", nullable = false, length = 20)
    private String vdContador;

    @NotNull
    @Column(name = "vd_vecino", nullable = false)
    private Integer vdVecino;


}