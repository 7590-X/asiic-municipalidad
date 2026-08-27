package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "as_testigos")
public class AsTestigo {
    @EmbeddedId
    private AsTestigoId id;

    @MapsId("tePersona")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "te_persona", nullable = false)
    private AsPersona tePersona;

    @MapsId("teInsidencia")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "te_insidencia", nullable = false)
    private AsInsidencia teInsidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "te_correo")
    private AsCorreo teCorreo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "te_telefono", nullable = false)
    private AsTelefono teTelefono;


}