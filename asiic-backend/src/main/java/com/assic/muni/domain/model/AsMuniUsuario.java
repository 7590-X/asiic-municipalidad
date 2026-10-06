package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "as_munis_usuarios")
public class AsMuniUsuario {
    @EmbeddedId
    private AsMuniUsuarioId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mu_muni", insertable = false, updatable = false)
    private AsMuni muMuni;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mu_usuario", insertable = false, updatable = false)
    private AsUsuario muUsuario;

    @Size(max = 1)
    @Column(name = "mu_estado", length = 1)
    private String muEstado;

    @CreatedDate
    @Column(name = "mu_fec_registro")
    private Instant muFecRegistro;

    @CreatedBy
    @Size(max = 36)
    @Column(name = "mu_usr_registro", length = 36)
    private String muUsrRegistro;

    @LastModifiedDate
    @Column(name = "mu_fec_modifico")
    private Instant muFecModifico;

    @Size(max = 36)
    @LastModifiedBy
    @Column(name = "mu_usr_modifico", length = 36)
    private String muUsrModifico;
}