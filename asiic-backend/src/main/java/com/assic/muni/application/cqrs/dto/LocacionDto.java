package com.assic.muni.application.cqrs.dto;

import com.assic.muni.domain.model.AsLocacion;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LocacionDto(
        @JsonProperty("id")
        int id,

        @JsonProperty("pais_id")
        Short paisId,

        @JsonProperty("depto_id")
        Short deptoId,

        @JsonProperty("muni_id")
        Short muniId,

        @JsonProperty("comuna_id")
        Short comunaId,

        @JsonProperty("nacionalidad")
        String nacionalidad,

        @JsonProperty("zipcode")
        String zipCode,

        @JsonProperty("descripcion")
        String descripcion
) {
    public LocacionDto(AsLocacion l) {
        this(
                l.getId(), l.getLoPais(), l.getLoDepto(), l.getLoMuni(),
                l.getLoComuna(), l.getLoNacionalidad(), l.getLoZipcode(),
                l.getLoDescripcion());
    }
}