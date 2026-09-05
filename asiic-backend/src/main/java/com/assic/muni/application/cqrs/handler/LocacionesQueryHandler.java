package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.LocacionDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.model.AsLocacion;
import com.assic.muni.infrastructure.repository.AsLocacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocacionesQueryHandler {
    public final AsLocacionRepository locacionRepository;

    public List<LocacionDto> obtainPaises() {
        List<AsLocacion> locaciones = locacionRepository.findAllPaises();
        if (locaciones.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron países disponibles");
        }
        return locaciones.stream().map(LocacionDto::new).toList();
    }

    public List<LocacionDto> obtainDeptos(short paisId) {
        List<AsLocacion> locaciones = locacionRepository.findAllDeptos(paisId);
        if (locaciones.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron departamentos disponibles");
        }
        return locaciones.stream().map(LocacionDto::new).toList();
    }

    public List<LocacionDto> obtainMunis(short paisId, short deptoId) {
        List<AsLocacion> locaciones = locacionRepository.findAllMunis(paisId, deptoId);
        if (locaciones.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron municipalidades disponibles");
        }
        return locaciones.stream().map(LocacionDto::new).toList();
    }

    public List<LocacionDto>    obtainComunas(short paisId, short deptoId, short muniId) {
        List<AsLocacion> locaciones = locacionRepository.findAllComunas(paisId, deptoId, muniId);
        if (locaciones.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron comunas disponibles");
        }
        return locaciones.stream().map(LocacionDto::new).toList();
    }
}
