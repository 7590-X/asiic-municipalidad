package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.infrastructure.repository.AsCorreoRepository;
import com.assic.muni.infrastructure.repository.AsPersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NumerosQueryHandler {
    private final AsPersonaRepository asPersonaRepository;
    private final AsCorreoRepository asCorreoRepository;

    public void validarDisponibilidadCUI(String cui) {
        cui = cui.trim();
        boolean exists = asPersonaRepository.existsByPeCui(cui);
        if (exists) {
            throw new ServiceException(HttpStatus.CONFLICT, "El número de CUI " + cui + " ya se encuentra registrado");
        }
    }

    public void validarDisponibilidadNIT(String nit) {
        nit = nit.trim().toUpperCase();
        boolean exists = asPersonaRepository.existsByPeNit(nit);
        if (exists) {
            throw new ServiceException(HttpStatus.CONFLICT, "El número de NIT " + nit + " ya se encuentra registrado");
        }
    }

    public void validarDisponibilidadCorreo(String correo) {
        correo = correo.trim().toLowerCase();
        boolean exists = asCorreoRepository.existsByCoCorreo(correo);
        if (exists) {
            throw new ServiceException(HttpStatus.CONFLICT, "El correo ya se encuentra registrado");
        }
    }
}
