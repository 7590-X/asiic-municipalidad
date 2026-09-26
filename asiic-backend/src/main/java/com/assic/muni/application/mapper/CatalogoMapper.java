package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.dto.CatalogoItemDto;
import com.assic.muni.domain.model.AsCatalogo;

public final class CatalogoMapper {

    public static CatalogoItemDto fromEntityToDto(AsCatalogo entity){
        return new CatalogoItemDto(entity.getId(), entity.getCaValor(), entity.getCaSeudo());
    }

}
