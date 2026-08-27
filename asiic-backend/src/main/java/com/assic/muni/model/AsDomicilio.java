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
@Table(name = "as_domicilios")
public class AsDomicilio {
    @Id
    @Size(max = 20)
    @Column(name = "do_contador", nullable = false, length = 20)
    private String doContador;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "do_direccion", nullable = false)
    private AsDireccione doDireccion;

    @Size(max = 20)
    @Column(name = "do_latitud", length = 20)
    private String doLatitud;

    @Size(max = 20)
    @Column(name = "do_longitud", length = 20)
    private String doLongitud;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "do_fec_registro", nullable = false)
    private Instant doFecRegistro;

    @Column(name = "do_fec_modifico")
    private Instant doFecModifico;


}