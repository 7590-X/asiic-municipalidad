package com.assic.muni.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "as_locaciones")
public class AsLocacione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lo_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "lo_pais", nullable = false)
    private Short loPais;

    @NotNull
    @Column(name = "lo_depto", nullable = false)
    private Short loDepto;

    @NotNull
    @Column(name = "lo_muni", nullable = false)
    private Short loMuni;

    @NotNull
    @Column(name = "lo_comuna", nullable = false)
    private Short loComuna;

    @Size(max = 45)
    @Column(name = "lo_nacionalidad", length = 45)
    private String loNacionalidad;

    @Size(max = 10)
    @Column(name = "lo_zipcode", length = 10)
    private String loZipcode;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "lo_fec_registro", nullable = false)
    private Instant loFecRegistro;

    @Column(name = "lo_fec_modifico")
    private Instant loFecModifico;


}