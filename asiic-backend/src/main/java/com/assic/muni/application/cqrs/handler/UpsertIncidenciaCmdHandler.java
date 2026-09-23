package com.assic.muni.application.cqrs.handler;

import java.util.List;
import java.util.Map;

import com.assic.muni.application.util.JsonbConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.group.GrpQueja;
import com.assic.muni.application.mapper.IncidenciaMapper;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.application.util.PayloadValidator;
import com.assic.muni.domain.model.AsIncidencia;
import com.assic.muni.domain.repository.AsCatalogoRepository;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import com.assic.muni.domain.repository.AsUsuarioRepository;
import com.assic.muni.domain.repository.AsVecinoDomicilioRepository;
import lombok.RequiredArgsConstructor;

/**
 * Clase para la ejecución de comando UPSERT para el registro de una insidencia
 * en estado de borrador
 */
@Service
@RequiredArgsConstructor
public class UpsertIncidenciaCmdHandler {

    // Repositorios
    private final AsUsuarioRepository usuarioRepository;
    private final AsCatalogoRepository catalogoRepository;
    private final AsVecinoDomicilioRepository vecinoDomicilioRepository;
    private final AsIncidenciaRepository incidenciaRepository;

    // Utils
    private final PayloadValidator payloadValidator;
    private final JsonbConverter jsonbConverter;

    public Integer handle(IncidenciaPayloadCmd payload, List<MultipartFile> evidencias) {
        final String subject = JwtExtractor.extrarJwtSubject();
        return switch (payload.getTipoIncidencia()) {
            case 1 -> {
                payloadValidator.validate(payload, GrpQueja.class);
                yield registrarSolicitudQueja(payload, subject);
            }
            case 2 -> registrarSolicitudReclamo(payload, subject);
            case 3 -> registrarSolicitudDenuncia(payload, subject);
            case 4 -> registrarSolicitudSujerencia(payload, subject);
            default ->
                    throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo identificare el tipo de insidencia");
        };
    }

    private Integer registrarSolicitudQueja(IncidenciaPayloadCmd payload, String subject) {
        // Validaciones
        Integer vecinoId = verificacionExistenciaVecino(subject);
        validarPerteneciaDomicilio(payload.getContador(), vecinoId);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        final short unidadId = payload.getDetalleQueja().getDependenciaId();
        boolean existsUnidad = catalogoRepository.existsByTableAndId((short) 9, unidadId); // 9 = as_areas
        if (!existsUnidad) {
            throw new ServiceException(HttpStatus.BAD_REQUEST,
                    "No se pudo identificar la dependencia de la solicitud de queja");
        }
        // Validar UPSERT
        AsIncidencia insidencia = null;
        if (payload.getInsidenciaId() != null) {
            insidencia = incidenciaRepository.findByIdAndInVecino(payload.getInsidenciaId(), vecinoId)
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,
                            "La insidencia no fue encontrada para su actualización"));
        }

        // Construcción Template
        Map<String, Object> evidencias = jsonbConverter.convertPojoToMap(payload.getDetalleQueja().getTestigo());
        final AsIncidencia toPersist = IncidenciaMapper.frontDtoToQueja(
                privacidad, vecinoId, unidadId, payload, insidencia, evidencias
        );

        // Persistencia
        AsIncidencia persisted = incidenciaRepository.save(toPersist);

        // Guardado de archivos en SFTP

        // Eventos

        return persisted.getId();
    }

    private Integer registrarSolicitudReclamo(IncidenciaPayloadCmd payload, String subject) {
        Integer vecinoId = verificacionExistenciaVecino(subject);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        return null;
    }

    private Integer registrarSolicitudDenuncia(IncidenciaPayloadCmd payload, String subject) {
        Integer vecinoId = verificacionExistenciaVecino(subject);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        return null;
    }

    private Integer registrarSolicitudSujerencia(IncidenciaPayloadCmd payload, String subject) {
        Integer vecinoId = verificacionExistenciaVecino(subject);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        return null;
    }

    /**
     * Verifica si el usuario existe en base de datos y obtiene el ID del vecino
     *
     * @param uuid UUID de la cuenta autenticada
     * @return ID del vecino
     */
    private Integer verificacionExistenciaVecino(String uuid) {
        Integer vecinoId = usuarioRepository.findUsPersonaByUsId(uuid)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,
                        "No se pudo obtener información de la cuenta"));
        return vecinoId;
    }

    /**
     * Verificar la privacidad de la solicitud
     *
     * @param pseudo Pseudonimo de privacidad
     * @return ID de privacidad en catalogo
     */
    private Short verificacionExistenciaPrivacidad(String pseudo) {
        if (pseudo == "PUB" || pseudo == "PRIV") {
            return catalogoRepository.findIdByCaSeudo(pseudo)
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND,
                            "No se encontró el catalogo para el tipo de privacidad"));
        }
        throw new ServiceException(HttpStatus.BAD_REQUEST, "El pseudonimo de privacidad es incorrecto");
    }

    /**
     * Validar que el número de contador del vecino sea perteneciente a su cuenta
     *
     * @param contador Número de contador
     * @param vecinoId Código de vecino
     */
    private void validarPerteneciaDomicilio(String contador, int vecinoId) {
        boolean exists = vecinoDomicilioRepository.existsRelationByContadorAndVecino(contador, vecinoId);
        if (!exists) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo verificar la pertenencia de la residencia");
        }
    }
}
