package com.assic.muni.domain.model;

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
@Table(name = "as_vecinos")
public class AsVecino {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ve_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ve_correo", nullable = false)
    private AsCorreo veCorreo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ve_telefono", nullable = false)
    private AsTelefono veTelefono;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ve_profesion", nullable = false)
    private AsCatalogo veProfesion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ve_direccion", nullable = false)
    private AsDireccione veDireccion;

    @Size(max = 1)
    @NotNull
    @Column(name = "ve_estado", nullable = false, length = 1)
    private String veEstado;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ve_fec_registro", nullable = false)
    private Instant veFecRegistro;

    @Size(max = 32)
    @NotNull
    @Column(name = "ve_ip_registro", nullable = false, length = 32)
    private String veIpRegistro;

    @Column(name = "ve_fec_modifico")
    private Instant veFecModifico;

    @Size(max = 25)
    @Column(name = "ve_usr_modifico", length = 25)
    private String veUsrModifico;

    @Size(max = 32)
    @Column(name = "ve_ip_modifico", length = 32)
    private String veIpModifico;


}