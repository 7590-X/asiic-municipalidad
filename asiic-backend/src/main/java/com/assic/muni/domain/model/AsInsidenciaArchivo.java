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
@Table(name = "as_insidencias_archivos")
public class AsInsidenciaArchivo {
    @Id
    @Column(name = "ai_archivo", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_archivo", nullable = false)
    private AsArchivo asArchivos;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ia_insidencia", nullable = false)
    private AsInsidencia iaInsidencia;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ai_fec_registro", nullable = false)
    private Instant aiFecRegistro;

    @Size(max = 36)
    @NotNull
    @Column(name = "ai_usr_registro", nullable = false, length = 36)
    private String aiUsrRegistro;

    @Column(name = "ai_fec_modifico")
    private Instant aiFecModifico;

    @Size(max = 36)
    @Column(name = "ai_usr_modifico", length = 36)
    private String aiUsrModifico;

    @Size(max = 1)
    @NotNull
    @Column(name = "ai_estado", nullable = false, length = 1)
    private String aiEstado;


}