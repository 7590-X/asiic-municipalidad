package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "as_denunciados")
public class AsDenunciado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "de_id", nullable = false)
    private Integer id;

    @Size(max = 75)
    @NotNull
    @Column(name = "de_nombre", nullable = false, length = 75)
    private String deNombre;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "de_tipo", nullable = false)
    private AsCatalogo deTipo;

    @Size(max = 1)
    @NotNull
    @Column(name = "de_estado", nullable = false, length = 1)
    private String deEstado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "de_insidencia", nullable = false)
    private AsInsidencia deInsidencia;


}