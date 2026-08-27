package com.assic.muni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "as_tablas")
public class AsTabla {
    @Id
    @Column(name = "ta_id", nullable = false, precision = 2)
    private BigDecimal id;

    @Size(max = 45)
    @NotNull
    @Column(name = "ta_nombre", nullable = false, length = 45)
    private String taNombre;


}