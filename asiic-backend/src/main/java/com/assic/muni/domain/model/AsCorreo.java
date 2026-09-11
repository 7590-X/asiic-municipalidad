package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "as_correos")
@EntityListeners(AuditingEntityListener.class)
public class AsCorreo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "co_id", nullable = false)
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "co_correo", nullable = false, length = 45)
    private String coCorreo;

    @CreatedDate
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "co_fec_registro", nullable = false)
    private Instant coFecRegistro;

    @Size(max = 36)
    @NotNull
    @Column(name = "co_usr_registro", nullable = false, length = 36)
    private String coUsrRegistro;

    @Column(name = "co_fec_modifico")
    private Instant coFecModifico;

    @Size(max = 36)
    @Column(name = "co_usr_modifico", length = 36)
    private String coUsrModifico;


}