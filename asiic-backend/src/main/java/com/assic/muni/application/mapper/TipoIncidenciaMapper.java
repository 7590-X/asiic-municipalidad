package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.dto.TipoIncidenciaDto;
import com.assic.muni.domain.model.AsTipoInsidencia;

public final class TipoIncidenciaMapper {

    public static TipoIncidenciaDto fronEntityToDto(AsTipoInsidencia entity) {
        return new TipoIncidenciaDto(entity.getId(), entity.getTiNombre(), entity.getTiDescripcion());
    }

}
