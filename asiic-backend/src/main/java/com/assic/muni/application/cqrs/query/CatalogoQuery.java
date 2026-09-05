package com.assic.muni.application.cqrs.query;

import com.assic.muni.application.cqrs.dto.CatalogoItemDto;
import com.assic.muni.application.cqrs.enums.ECatalogo;

import java.util.List;

public interface CatalogoQuery {

    /**
     * Obtener listado de items de un catalogo almacenado en base de datos
     * @param catalogo ENUM del tipo de catálogo que se requiere obtener
     * @return Listado de items
     */
    List<CatalogoItemDto> getCatalogoItemsByCatalogoId(ECatalogo catalogo);

}
