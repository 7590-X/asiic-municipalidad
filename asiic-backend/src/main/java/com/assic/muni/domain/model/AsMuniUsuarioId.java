package com.assic.muni.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class AsMuniUsuarioId implements Serializable {
    @Serial
    private static final long serialVersionUID = 5980173283612793712L;
    @NotNull
    @Column(name = "mu_muni", nullable = false)
    private Short muMuni;

    @Size(max = 36)
    @NotNull
    @Column(name = "mu_usuario", nullable = false, length = 36)
    private String muUsuario;


}