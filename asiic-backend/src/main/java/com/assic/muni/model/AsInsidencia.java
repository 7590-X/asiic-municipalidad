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
@Table(name = "as_insidencias")
public class AsInsidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "in_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_privacidad", nullable = false)
    private AsCatalogo inPrivacidad;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_vecino", nullable = false)
    private AsVecino inVecino;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_contador", nullable = false)
    private AsDomicilio inContador;

    @Size(max = 100)
    @NotNull
    @Column(name = "in_direccion", nullable = false, length = 100)
    private String inDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_unidad")
    private AsCatalogo inUnidad;

    @Size(max = 100)
    @Column(name = "in_empleado", length = 100)
    private String inEmpleado;

    @NotNull
    @Column(name = "in_fec_insidencia", nullable = false)
    private Instant inFecInsidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_lugar")
    private AsCatalogo inLugar;

    @NotNull
    @Column(name = "in_comentario", nullable = false, length = Integer.MAX_VALUE)
    private String inComentario;

    @Size(max = 20)
    @Column(name = "in_latitud", length = 20)
    private String inLatitud;

    @Size(max = 20)
    @Column(name = "in_longitud", length = 20)
    private String inLongitud;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_estado", nullable = false)
    private AsInsidenciaEstado inEstado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_tipo_servicio", nullable = false)
    private AsCatalogo inTipoServicio;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "in_tipo_insidencia", nullable = false)
    private AsTipoInsidencia inTipoInsidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_tipo_denuncia")
    private AsCatalogo inTipoDenuncia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_area")
    private AsCatalogo inArea;

    @NotNull
    @Column(name = "in_propuesta", nullable = false, length = Integer.MAX_VALUE)
    private String inPropuesta;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "in_fec_registro", nullable = false)
    private Instant inFecRegistro;

    @Size(max = 25)
    @NotNull
    @Column(name = "in_usr_registro", nullable = false, length = 25)
    private String inUsrRegistro;

    @Column(name = "in_fec_modifico")
    private Instant inFecModifico;

    @Size(max = 25)
    @Column(name = "in_usr_modifico", length = 25)
    private String inUsrModifico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "in_analista")
    private AsUsuario inAnalista;


}