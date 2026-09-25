package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.dto.ArchivoDto;
import com.assic.muni.domain.model.AsArchivo;

public final class ArchivoMapper {

    public static ArchivoDto frontEntityToDto(AsArchivo archivo) {
        return new ArchivoDto(
                archivo.getId(),
                archivo.getArNombre(),
                archivo.getArFormato(),
                archivo.getArFecRegistro()
        );
    }

}
