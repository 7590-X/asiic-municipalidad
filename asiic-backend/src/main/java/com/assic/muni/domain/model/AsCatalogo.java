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
@Table(name = "as_catalogos")
public class AsCatalogo {
    @Id
    @Column(name = "ca_id", nullable = false)
    private Short id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ca_tabla", nullable = false)
    private AsTabla caTabla;

    @Size(max = 100)
    @NotNull
    @Column(name = "ca_valor", nullable = false, length = 100)
    private String caValor;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ca_fec_registro", nullable = false)
    private Instant caFecRegistro;


}