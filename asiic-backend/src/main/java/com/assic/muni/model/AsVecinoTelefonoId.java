package com.assic.muni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class AsVecinoTelefonoId implements Serializable {
    @Serial
    private static final long serialVersionUID = -8684337797610816637L;
    @NotNull
    @Column(name = "vt_vecino", nullable = false)
    private Integer vtVecino;

    @NotNull
    @Column(name = "vt_telefono", nullable = false)
    private Integer vtTelefono;


}