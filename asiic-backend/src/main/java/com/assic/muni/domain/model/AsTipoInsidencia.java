package com.assic.muni.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "as_tipo_insidencia")
public class AsTipoInsidencia {
    @Id
    @Column(name = "ti_id", nullable = false, precision = 2)
    private BigDecimal id;

    @Size(max = 30)
    @NotNull
    @Column(name = "ti_nombre", nullable = false, length = 30)
    private String tiNombre;

    @Size(max = 75)
    @NotNull
    @Column(name = "ti_descripcion", nullable = false, length = 75)
    private String tiDescripcion;

    @Size(max = 1)
    @NotNull
    @Column(name = "ti_estado", nullable = false, length = 1)
    private String tiEstado;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ti_fec_registro", nullable = false)
    private Instant tiFecRegistro;

    @Column(name = "ti_fec_modifico")
    private Instant tiFecModifico;


}