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
@Table(name = "as_documentos_investigacion")
public class AsDocumentoInvestigacion {
    @Id
    @Column(name = "di_archivo", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "di_archivo", nullable = false)
    private AsArchivo asArchivos;

    @NotNull
    @JoinColumns({
            @JoinColumn(name = "di_secuencia",
                    referencedColumnName = "ic_secuancial",
                    nullable = false),
            @JoinColumn(name = "di_insidencia",
                    referencedColumnName = "ic_insidencia",
                    nullable = false)})
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AsInvestigacionCampo asInvestigacionesCampo;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "di_fec_registro", nullable = false)
    private Instant diFecRegistro;

    @Size(max = 20)
    @NotNull
    @Column(name = "di_usr_registro", nullable = false, length = 20)
    private String diUsrRegistro;

    @Column(name = "di_fec_modifico")
    private Instant diFecModifico;

    @Size(max = 20)
    @Column(name = "di_usr_modifico", length = 20)
    private String diUsrModifico;

    @Size(max = 1)
    @NotNull
    @Column(name = "di_estado", nullable = false, length = 1)
    private String diEstado;


}