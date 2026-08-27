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
@Table(name = "as_investigaciones_campo")
public class AsInvestigacionCampo {
    @EmbeddedId
    private AsInvestigacionCampoId id;

    @MapsId("icInsidencia")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ic_insidencia", nullable = false)
    private AsInsidencia icInsidencia;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ic_agente", nullable = false)
    private AsUsuario icAgente;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ic_fec_registro", nullable = false)
    private Instant icFecRegistro;

    @Size(max = 20)
    @NotNull
    @Column(name = "ic_usr_registro", nullable = false, length = 20)
    private String icUsrRegistro;

    @Column(name = "ic_fec_modifico")
    private Instant icFecModifico;

    @Size(max = 20)
    @Column(name = "ic_usr_modifico", length = 20)
    private String icUsrModifico;

    @Size(max = 1)
    @NotNull
    @Column(name = "ic_estado", nullable = false, length = 1)
    private String icEstado;


}