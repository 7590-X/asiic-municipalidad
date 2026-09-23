package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.GetVecinoMeQuery;
import com.assic.muni.application.cqrs.dto.VecinoMeDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.model.AsVecino;
import com.assic.muni.domain.model.AsVecinoDomicilio;
import com.assic.muni.domain.repository.AsVecinoDomicilioRepository;
import com.assic.muni.domain.repository.VecinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetVecinoMeQueryHandler implements CQRSCmdHandler<VecinoMeDto, GetVecinoMeQuery> {

    private final VecinoRepository vecinoRepository;
    private final AsVecinoDomicilioRepository vecinoDomicilioRepository;

    @Override
    public VecinoMeDto handle(GetVecinoMeQuery query) {
        AsVecino vecino = vecinoRepository.findByVeUsrRegistro(query.getUserId())
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "No se encontró el perfil del vecino"));

        Optional<AsVecinoDomicilio> domicilioOpt = vecinoDomicilioRepository.findFirstByVdVecinoId(vecino.getId());
        String direccion = domicilioOpt
                .map(d -> d.getVdDomicilio().getDoDireccion().getDiDireccion())
                .orElse("No registrada");

        String dpi = vecino.getVePersona() != null ? vecino.getVePersona().getPeCui() : "No registrado";
        String nombresApellidos = vecino.getVePersona() != null 
                ? vecino.getVePersona().getPeNombre() + " " + vecino.getVePersona().getPeApellido() 
                : "Vecino Municipal";

        String correo = vecino.getVeCorreo() != null ? vecino.getVeCorreo().getCoCorreo() : "";
        String telefono = vecino.getVeTelefono() != null ? vecino.getVeTelefono().getTeTelefono() : "No registrado";

        return VecinoMeDto.builder()
                .id(vecino.getId())
                .dpi(dpi)
                .nombresApellidos(nombresApellidos)
                .correo(correo)
                .telefono(telefono)
                .direccion(direccion)
                .build();
    }
}
