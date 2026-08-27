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
@Table(name = "as_direcciones")
public class AsDireccione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "di_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "di_direccion", nullable = false, length = 100)
    private String diDireccion;

    @Size(max = 1)
    @NotNull
    @Column(name = "di_tipo", nullable = false, length = 1)
    private String diTipo;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "di_fec_registro", nullable = false)
    private Instant diFecRegistro;

    @Column(name = "di_fec_modifico")
    private Instant diFecModifico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "di_locacion")
    private AsLocacione diLocacion;


}