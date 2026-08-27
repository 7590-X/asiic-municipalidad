package com.assic.muni.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class AsInvestigacionCampoId implements Serializable {
    @Serial
    private static final long serialVersionUID = -8128032549223222161L;
    @NotNull
    @Column(name = "ic_secuancial", nullable = false, precision = 2)
    private BigDecimal icSecuancial;

    @NotNull
    @Column(name = "ic_insidencia", nullable = false)
    private Integer icInsidencia;


}