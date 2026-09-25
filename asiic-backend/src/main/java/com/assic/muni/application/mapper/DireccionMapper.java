package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.dto.DireccionDto;
import com.assic.muni.domain.model.AsDireccion;
import com.assic.muni.domain.model.AsLocacion;

public final class DireccionMapper {

    public static DireccionDto fromEntityToDto(AsDireccion entity) {
        AsLocacion locacion = entity.getDiLocacionObj();
        return new DireccionDto(entity.getId(), entity.getDiDireccion(), null, null, null, locacion.getLoDescripcion());
    }

}
