package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.dto.VecinoDto;
import com.assic.muni.domain.model.AsVecino;

public final class VecinoMapper {

    public static VecinoDto fronEntityToDto(AsVecino entity) {
        String cui = entity.getVePersona().getPeCui();
        String nombreCompleto = (entity.getVePersona().getPeNombre() + " " + entity.getVePersona().getPeApellido()).trim();;
        String correo = entity.getVeCorreo() != null ? entity.getVeCorreo().getCoCorreo() : null;
        String telefono = entity.getVeTelefono() != null ? entity.getVeTelefono().getTeTelefono() : null;
        return new VecinoDto(entity.getId(), cui, nombreCompleto, correo, telefono);
    }
}
