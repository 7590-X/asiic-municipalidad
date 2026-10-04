package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.MuniDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.repository.AsMuniRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MunisQueryHandler {

    private final AsMuniRepository muniRepository;

    public List<MuniDto> obtenerMunis(){
        List<MuniDto> munis = muniRepository.findAllMunis();
        if(munis.isEmpty()){
            throw new ServiceException(HttpStatus.NOT_FOUND,"No se encontraron municipalidades registradas");
        }
        return munis;
    }

    public MuniDto obtenerMuniById(Short muniId){
        return muniRepository.findMuniDtoById(muniId)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND,"No se encontró la municipalidad con el id: "+muniId));
    }
}
