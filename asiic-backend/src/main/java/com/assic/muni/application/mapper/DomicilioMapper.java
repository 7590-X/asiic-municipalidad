package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.dto.DomicilioDto;
import com.assic.muni.domain.model.AsDomicilio;

public class DomicilioMapper {

    public static DomicilioDto frontEntityToDto(AsDomicilio entity) {
        return new DomicilioDto(entity.getDoContador(), entity.getDoLatitud(), entity.getDoLongitud(), DireccionMapper.fromEntityToDto(entity.getDoDireccion()));
    }

}
