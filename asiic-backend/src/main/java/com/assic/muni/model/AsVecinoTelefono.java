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
@Table(name = "as_vecinos_telefonos")
public class AsVecinoTelefono {
    @EmbeddedId
    private AsVecinoTelefonoId id;

    @MapsId("vtVecino")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vt_vecino", nullable = false)
    private AsVecino vtVecino;

    @MapsId("vtTelefono")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vt_telefono", nullable = false)
    private AsTelefono vtTelefono;

    @Size(max = 1)
    @NotNull
    @Column(name = "vt_estado", nullable = false, length = 1)
    private String vtEstado;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "vt_fec_registro", nullable = false)
    private Instant vtFecRegistro;

    @Size(max = 25)
    @NotNull
    @Column(name = "vt_usr_registro", nullable = false, length = 25)
    private String vtUsrRegistro;

    @Column(name = "vt_fec_modifico")
    private Instant vtFecModifico;

    @Size(max = 25)
    @Column(name = "vt_usr_modifico", length = 25)
    private String vtUsrModifico;


}