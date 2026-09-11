package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "as_telefonos")
@EntityListeners(AuditingEntityListener.class)
public class AsTelefono {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "te_id", nullable = false)
    private Integer id;

    @Size(max = 15)
    @NotNull
    @Column(name = "te_telefono", nullable = false, length = 15)
    private String teTelefono;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "te_fec_registro", nullable = false)
    private Instant teFecRegistro;

    @Size(max = 36)
    @NotNull
    @Column(name = "te_usr_registro", nullable = false, length = 25)
    private String teUsrRegistro;


}