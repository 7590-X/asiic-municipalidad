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
@Table(name = "as_direcciones")
@EntityListeners(AuditingEntityListener.class)
public class AsDireccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "di_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "di_direccion", nullable = false, length = 100)
    private String diDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "di_tipo", insertable = false, updatable = false)
    private AsCatalogo diTipoObj;

    @Column(name = "di_tipo")
    private Integer diTipo;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "di_fec_registro", nullable = false)
    private Instant diFecRegistro;

    @LastModifiedDate
    @Column(name = "di_fec_modifico")
    private Instant diFecModifico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "di_locacion", insertable = false, updatable = false)
    private AsLocacion diLocacionObj;

    @Column(name = "di_locacion")
    private Integer diLocacion;


}