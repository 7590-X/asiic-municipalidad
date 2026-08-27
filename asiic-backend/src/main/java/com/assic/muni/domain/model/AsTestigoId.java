package com.assic.muni.domain.model;

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
public class AsTestigoId implements Serializable {
    @Serial
    private static final long serialVersionUID = -912551984822201626L;
    @NotNull
    @Column(name = "te_persona", nullable = false)
    private Integer tePersona;

    @NotNull
    @Column(name = "te_insidencia", nullable = false)
    private Integer teInsidencia;


}