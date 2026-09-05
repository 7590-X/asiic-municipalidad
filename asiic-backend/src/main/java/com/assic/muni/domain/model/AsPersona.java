package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "as_personas")
public class AsPersona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pe_id", nullable = false)
    private Integer id;

    @Size(max = 13)
    @Column(name = "pe_cui", length = 13)
    private String peCui;

    @Size(max = 45)
    @NotNull
    @Column(name = "pe_nombre", nullable = false, length = 45)
    private String peNombre;

    @Size(max = 45)
    @NotNull
    @Column(name = "pe_apellido", nullable = false, length = 45)
    private String peApellido;

    @Size(max = 12)
    @Column(name = "pe_nit", length = 13)
    private String peNit;

    @Size(max = 20)
    @Column(name = "pe_pasaporte", length = 20)
    private String pePasaporte;

    @Size(max = 1)
    @NotNull
    @Column(name = "pe_genero", nullable = false, length = 1)
    private String peGenero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pe_estado_civil", insertable = false, updatable = false)
    private AsCatalogo peEstadoCivilObj;

    @Column(name = "pe_estado_civil")
    private Short peEstadoCivil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pe_tip_persona", insertable = false, updatable = false)
    private AsCatalogo peTipPersonaObj;

    @Column(name = "pe_tip_persona")
    private Short peTipPersona;


}