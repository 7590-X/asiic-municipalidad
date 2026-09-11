package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "as_usuarios")
public class AsUsuario {

    @Id
    @Size(max = 36)
    @Column(name = "us_id", nullable = false, length = 36)
    private String usId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "us_tipo", nullable = false)
    private AsCatalogo usTipo;

    @Size(max = 1)
    @NotNull
    @Column(name = "us_estado", nullable = false, length = 1)
    private String usEstado;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "us_fec_registro", nullable = false)
    private Instant usFecRegistro;

    @Size(max = 36)
    @NotNull
    @Column(name = "us_ip_registro", nullable = false, length = 36)
    private String usIpRegistro;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "us_persona", nullable = false)
    private AsPersona usPersona;

    @Size(max = 36)
    @NotNull
    @Column(name = "us_usr_registro", nullable = false, length = 36)
    private String usUsrRegistro;

    @LastModifiedDate
    @Column(name = "us_fec_modifico")
    private Instant usFecModifico;

    @Size(max = 32)
    @Column(name = "us_ip_modifico", length = 32)
    private String usIpModifico;

    @Size(max = 36)
    @Column(name = "us_usr_modifico", length = 36)
    private String usUsrModifico;
}