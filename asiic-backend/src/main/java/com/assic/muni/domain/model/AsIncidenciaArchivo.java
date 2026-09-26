package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "as_insidencias_archivos")
public class AsIncidenciaArchivo {

    @EmbeddedId
    private AsIncidenciaArchivoId id;

    //    @MapsId("aiArchivo")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_archivo", insertable = false, updatable = false)
    private AsArchivo asArchivo;

    //    @MapsId("aiIncidencia")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ia_insidencia", insertable = false, updatable = false)
    private AsIncidencia asIncidencia;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ai_fec_registro", nullable = false)
    private Instant aiFecRegistro;

    @Size(max = 36)
    @CreatedBy
    @Column(name = "ai_usr_registro", nullable = false, length = 36)
    private String aiUsrRegistro;

    @LastModifiedDate
    @Column(name = "ai_fec_modifico")
    private Instant aiFecModifico;

    @Size(max = 36)
    @LastModifiedBy
    @Column(name = "ai_usr_modifico", length = 36)
    private String aiUsrModifico;

    @Size(max = 1)
    @NotNull
    @Column(name = "ai_estado", nullable = false, length = 1)
    private String aiEstado;
}