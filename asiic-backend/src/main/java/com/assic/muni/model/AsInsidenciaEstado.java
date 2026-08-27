package com.assic.muni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "as_insidencias_estados")
public class AsInsidenciaEstado {
    @Id
    @Size(max = 30)
    @Column(name = "ie_id", nullable = false, length = 30)
    private String ieId;

    @Size(max = 45)
    @NotNull
    @Column(name = "ie_nombre", nullable = false, length = 45)
    private String ieNombre;

    @Size(max = 1)
    @NotNull
    @Column(name = "ie_estado", nullable = false, length = 1)
    private String ieEstado;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ie_fec_registro", nullable = false)
    private Instant ieFecRegistro;

    @Column(name = "ie_fec_modifico")
    private Instant ieFecModifico;


}