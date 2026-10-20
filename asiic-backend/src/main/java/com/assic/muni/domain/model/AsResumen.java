package com.assic.muni.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "as_resumen")
@EntityListeners(AuditingEntityListener.class)
public class AsResumen {

    @EmbeddedId
    private AsResumenId id;

    @Column(name = "re_body", length = Integer.MAX_VALUE)
    private String reBody;

    @Column(name = "re_fec_registro")
    private Instant reFecRegistro;

    @Size(max = 36)
    @Column(name = "re_usr_registro", length = 36)
    private String reUsrRegistro;

    @Column(name = "re_latitud")
    private String reLatitud;

    @Column(name = "re_longitud")
    private String reLongitud;

}