package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.MuniPayloadCmd;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.model.*;
import com.assic.muni.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Handler encargado de crear y actualizar una municipalidad junto con sus entidades
 * relacionadas (correo, dirección y persona).
 *
 * @author rmachicm
 */
@Service
@RequiredArgsConstructor
public class UpsertMunicipalidadCmdHandler implements CQRSCmdHandler<Short, MuniPayloadCmd> {

    private final AsLocacionRepository locacionRepository;
    private final AsMuniRepository muniRepository;
    private final AsCorreoRepository correoRepository;
    private final AsDireccionRepository direccionRepository;
    private final AsPersonaRepository personaRepository;
    private final AsCatalogoRepository catalogoRepository;

    @Override
    @Transactional
    public Short handle(MuniPayloadCmd cmd) {
        AsMuni muni = null;
        if (null == cmd.municipalidadId()) {
            int vReturn = muniRepository.validarMunicipalidad(cmd.correo(), cmd.municipioId(), cmd.nit());
            String errorMessage =
                    switch (vReturn) {
                        case 1 -> "El correo ya fue registrado por otra municipalidad";
                        case 2 -> "Otra municipalidad ya fue registrada en el mismo municipio";
                        case 3 -> "Otra municipalidad ya registro el número de NIT";
                        default -> null;
                    };
            if (null != errorMessage) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, errorMessage);
            }
            muni = new AsMuni();
        } else {
            muni = muniRepository.findById(cmd.municipalidadId())
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "La municipalidad no existe"));
        }
        // Validación del municipio asociado a la municipalidad
        boolean existsMunicipio = locacionRepository.existsByLoMuni(cmd.municipioId());
        if (!existsMunicipio) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "El municipio asociado a la municipalidad no existe");
        }

        AsCorreo correo = upsertCorreo(cmd.municipalidadId(), cmd.correo());
        AsDireccion direccion = upsertDireccion(cmd.municipioId(), cmd.direccionFiscal(), cmd.municipalidadId());
        AsCatalogo tipoPersona = catalogoRepository.findByCaSeudo("PEJU")
                .orElseThrow(() -> new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un problema al tratar de identificar el tipo de persona"));
        AsPersona persona = upsertPersona(cmd.municipalidadId(), cmd.nombre(), cmd.nit(), tipoPersona.getId());

        if (null == cmd.municipalidadId()) {
            muni.setMuEstado("A");
        }
        muni.setMuFecFundacion(cmd.fechaFundacion());
        muni.setMuLatitud(cmd.latitudGps());
        muni.setMuLongitud(cmd.longitudGps());
        muni.setMuPbx(cmd.pbx());
        muni.setMuCorreo(correo.getId());
        muni.setMuDireccion(direccion.getId());
        muni.setMuPersona(persona.getId());
        AsMuni persisted = muniRepository.save(muni);
        return persisted.getId();
    }

    private AsCorreo upsertCorreo(Short muniId, String correo) {
        AsCorreo persisted;
        if (muniId != null) {
            persisted = correoRepository.findByCoCorreo(correo.toLowerCase())
                    .orElse(null);
            if (persisted == null) {
                persisted = correoRepository.findByJoinAsMuni(muniId)
                        .orElse(null);
            }

            if (persisted == null) {
                throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo actualizar el correo");
            }
            persisted.setCoCorreo(correo);
        } else {
            persisted = AsCorreo.builder()
                    .coCorreo(correo)
                    .build();
        }
        return correoRepository.save(persisted);
    }

    private AsDireccion upsertDireccion(int locacionId, String direccion, Short muniId) {
        AsDireccion persisted;
        if (muniId != null) {
            persisted = direccionRepository.findByJoinMuni(muniId)
                    .orElse(null);
            if (null != persisted) {
                persisted.setDiDireccion(direccion);
                persisted.setDiLocacion(locacionId);
            } else {
                throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo actualizar la dirección");
            }
        } else {
            persisted = AsDireccion.builder()
                    .diDireccion(direccion)
                    .diLocacion(locacionId)
                    .build();
        }
        return direccionRepository.save(persisted);
    }

    private AsPersona upsertPersona(Short muniId, String nombre, String nit, short tipoPersona) {
        AsPersona upsert;
        if (muniId != null) {
            upsert = personaRepository.findByJoinMuni(muniId)
                    .orElse(null);
            if (upsert != null) {
                upsert.setPeNombre(nombre);
                upsert.setPeNit(nit);
                upsert.setPeTipPersona(tipoPersona);
            } else {
                throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo actualizar la información municipal");
            }
        } else {
            upsert = AsPersona.builder()
                    .peNombre(nombre)
                    .peNit(nit)
                    .peTipPersona(tipoPersona)
                    .build();
        }
        return personaRepository.save(upsert);
    }
}
