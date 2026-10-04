package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "as_munis")
@EntityListeners(AuditingEntityListener.class)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mu_correo", insertable = false, updatable = false)
    private AsCorreo asCorreo;

    @NotNull
    @Column(name = "mu_correo", nullable = false)
    private Integer muCorreo;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "mu_fec_registro", nullable = false)
    private Instant muFecRegistro;

    @LastModifiedDate
    @Column(name = "mu_fec_modifico")
    private Instant muFecModifico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mu_direccion", insertable = false, updatable = false)
    private AsDireccion asDireccion;

    @Column(name = "mu_direccion")
    private Integer muDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mu_persona", insertable = false, updatable = false)
    private AsPersona asPersona;

    @NotNull
    @Column(name = "mu_persona")
    private Integer muPersona;
}