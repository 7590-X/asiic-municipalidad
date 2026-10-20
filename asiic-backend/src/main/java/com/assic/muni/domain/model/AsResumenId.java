package com.assic.muni.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AsResumenId implements Serializable {
    @Serial
    private static final long serialVersionUID = -5033003835240724389L;
    @NotNull
    @Column(name = "re_incidencia", nullable = false)
    private Integer reIncidencia;

    @NotNull
    @Column(name = "re_version", nullable = false)
    private Short reVersion;
}