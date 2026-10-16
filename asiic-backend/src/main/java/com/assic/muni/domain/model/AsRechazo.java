package com.assic.muni.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "as_rechazos")
public class AsRechazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "re_id", nullable = false)
    private Integer reId;

    @NotNull
    @Column(name = "re_incidencia", nullable = false)
    private Integer reIncidencia;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "re_incidencia", insertable = false, updatable = false)
    private AsIncidencia asIncidencia;

    @NotNull
    @Column(name = "re_numero", nullable = false, precision = 1)
    private Short reNumero;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "re_datos", nullable = false)
    private JsonNode reDatos;
}