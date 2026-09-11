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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "as_vecinos")
@EntityListeners(AuditingEntityListener.class)
public class AsVecino {

    @Id
    @Column(name = "ve_id", nullable = false)
    private Integer id;

    @NotNull
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ve_id")
    private AsPersona vePersona;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ve_correo", nullable = false)
    private AsCorreo veCorreo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ve_telefono", nullable = false)
    private AsTelefono veTelefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ve_profesion", insertable = false, updatable = false)
    private AsCatalogo veProfesionObj;

    @Column(name = "ve_profesion")
    private Short veProfesion;

    @Size(max = 1)
    @NotNull
    @Column(name = "ve_estado", nullable = false, length = 1)
    private String veEstado;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "ve_fec_registro", nullable = false)
    private Instant veFecRegistro;

    @Size(max = 36)
    @NotNull
    @Column(name = "ve_usr_registro", nullable = false, length = 36)
    private String veUsrRegistro;

    @Size(max = 32)
    @NotNull
    @Column(name = "ve_ip_registro", nullable = false, length = 32)
    private String veIpRegistro;

    @LastModifiedDate
    @Column(name = "ve_fec_modifico")
    private Instant veFecModifico;

    @Size(max = 36)
    @Column(name = "ve_usr_modifico", length = 36)
    private String veUsrModifico;

    @Size(max = 32)
    @Column(name = "ve_ip_modifico", length = 32)
    private String veIpModifico;

}