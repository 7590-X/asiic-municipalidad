package com.assic.muni.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "as_munis")
public class AsMuni {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mu_id", nullable = false)
    private Short id;

    @Size(max = 1)
    @NotNull
    @Column(name = "mu_estado", nullable = false, length = 1)
    private String muEstado;

    @Column(name = "mu_fec_fundacion")
    private LocalDate muFecFundacion;

    @Size(max = 20)
    @Column(name = "mu_latitud", length = 20)
    private String muLatitud;

    @Size(max = 20)
    @Column(name = "mu_longitud", length = 20)
    private String muLongitud;

    @Size(max = 12)
    @Column(name = "mu_pbx", length = 12)
    private String muPbx;

    @NotNull
    @Column(name = "mu_correo", nullable = false)
    private Integer muCorreo;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "mu_fec_registro", nullable = false)
    private Instant muFecRegistro;

    @Column(name = "mu_fec_modifico")
    private Instant muFecModifico;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mu_direccion", nullable = false)
    private AsDireccione muDireccion;


}