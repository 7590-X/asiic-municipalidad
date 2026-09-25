package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.DomicilioDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.mapper.DomicilioMapper;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.domain.model.AsDomicilio;
import com.assic.muni.domain.repository.AsDomicilioRepository;
import com.assic.muni.domain.repository.AsUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomicilioQueryHandler {

    private final AsDomicilioRepository domicilioRepository;
    private final AsUsuarioRepository usuarioRepository;

    public List<DomicilioDto> obtenerMisDomicilios() {
        int userId = obtenerUserId();
        List<AsDomicilio> domicilios = domicilioRepository.findByVecinoId(userId);
        if (domicilios.isEmpty()) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "No se encontraron domicilios");
        }
        return domicilios.stream().map(DomicilioMapper::frontEntityToDto).toList();
    }

    public DomicilioDto obtenerMiDomicilio(String contador) {
        int userId = obtenerUserId();
        return domicilioRepository.findByVecinoIdAndContador(userId, contador)
                .stream().map(DomicilioMapper::frontEntityToDto)
                .findFirst()
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "No se encontró el domicilio"));
    }

    public Integer obtenerUserId() {
        String subject = JwtExtractor.extrarJwtSubject();
        return usuarioRepository.findUsPersonaByUsId(subject)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo verificar la identidad del usuario"));
    }
}
