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
@Table(name = "as_telefonos")
public class AsTelefono {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "te_id", nullable = false)
    private Integer id;

    @Size(max = 15)
    @NotNull
    @Column(name = "te_telefono", nullable = false, length = 15)
    private String teTelefono;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "te_fec_registro", nullable = false)
    private Instant teFecRegistro;

    @Size(max = 25)
    @NotNull
    @Column(name = "te_usr_registro", nullable = false, length = 25)
    private String teUsrRegistro;


}