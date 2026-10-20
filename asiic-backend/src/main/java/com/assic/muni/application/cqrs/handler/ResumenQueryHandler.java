package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.ResumenDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.domain.model.AsResumen;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import com.assic.muni.domain.repository.AsResumenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumenQueryHandler {

    private final AsResumenRepository resumenRepository;
    private final AsIncidenciaRepository incidenciaRepository;

    public List<ResumenDto> obtenerResumenIncidencia(int incidenciaId) {
        final String userId = JwtExtractor.extrarJwtSubject();
        final boolean pertenece = incidenciaRepository.existsByIdAndInAnalista(incidenciaId, userId);
        if (!pertenece) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "La incidencia no pertenece al analista");
        }
        List<AsResumen> resumenes = resumenRepository.findById_ReIncidencia(incidenciaId);
        if (resumenes.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron resumenes para la incidencia");
        }
        return resumenes.stream().map((r) -> new ResumenDto(r.getReBody(), r.getReLatitud(), r.getReLongitud())).toList();
    }

}
