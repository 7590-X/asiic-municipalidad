package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.MuniDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.repository.AsMuniRepository;
import com.assic.muni.domain.repository.AsMuniUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MunisQueryHandler {

    private final AsMuniRepository muniRepository;
    private final AsMuniUsuarioRepository muniUsuarioRepository;

    public List<MuniDto> obtenerMunis() {
        List<MuniDto> munis = muniRepository.findAllMunis();
        if (munis.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron municipalidades registradas");
        }
        return munis;
    }

    public MuniDto obtenerMuniById(Short muniId) {
        return muniRepository.findMuniDtoById(muniId)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "No se encontró la municipalidad con el id: " + muniId));
    }

    public List<MuniDto> obtenerMunisAsignadasToUsuario(String userId) {
        List<MuniDto> munis = muniUsuarioRepository.findByAllMuniDtoByUsuarioId(userId);
        if (munis.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron municipalidades asignadas al usuario");
        }
        return munis;
    }
}
