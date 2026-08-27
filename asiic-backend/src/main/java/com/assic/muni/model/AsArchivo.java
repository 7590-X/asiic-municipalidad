package com.assic.muni.model;

import jakarta.persistence.*;
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
@Table(name = "as_archivos")
public class AsArchivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ar_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "ar_path", nullable = false, length = Integer.MAX_VALUE)
    private String arPath;

    @NotNull
    @Column(name = "ar_nombre", nullable = false, length = Integer.MAX_VALUE)
    private String arNombre;

    @Size(max = 5)
    @NotNull
    @Column(name = "ar_formato", nullable = false, length = 5)
    private String arFormato;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ar_fec_registro", nullable = false)
    private Instant arFecRegistro;

    @NotNull
    @Column(name = "ar_version", nullable = false, precision = 1)
    private BigDecimal arVersion;

    @Size(max = 64)
    @NotNull
    @Column(name = "ar_hash", nullable = false, length = 64)
    private String arHash;
}