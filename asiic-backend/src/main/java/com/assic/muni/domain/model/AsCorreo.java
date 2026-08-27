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
@Table(name = "as_correos")
public class AsCorreo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "co_id", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "co_correo", nullable = false, length = 45)
    private String coCorreo;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "co_fec_registro", nullable = false)
    private Instant coFecRegistro;

    @Size(max = 25)
    @NotNull
    @Column(name = "co_usr_registro", nullable = false, length = 25)
    private String coUsrRegistro;

    @Column(name = "co_fec_modifico")
    private Instant coFecModifico;

    @Size(max = 25)
    @Column(name = "co_usr_modifico", length = 25)
    private String coUsrModifico;


}