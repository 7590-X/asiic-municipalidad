package com.assic.muni.application.cqrs.handler;

import java.util.List;

import com.assic.muni.application.port.out.FileStoragePort;
import com.assic.muni.domain.event.IncidenciaUpsertEvent;
import com.assic.muni.domain.model.*;
import com.assic.muni.domain.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.group.GrpQueja;
import com.assic.muni.application.mapper.IncidenciaMapper;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.application.util.PayloadValidator;
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
    private final AsIncidenciaArchivoRepository incidenciaArchivoRepository;

    // Puertos
    private final FileStoragePort fileStoragePort;

    // Eventos
    private final ApplicationEventPublisher eventPublisher;

    // Utils
    private final PayloadValidator payloadValidator;
    private final ObjectMapper objectMapper;
    private final VecinoRepository vecinoRepository;

    @Transactional
    public Integer handle(IncidenciaPayloadCmd payload, List<MultipartFile> evidencias) {
        final String subject = JwtExtractor.extrarJwtSubject();
        // Persistir incidencias
        int incidencia = switch (payload.getTipoIncidencia()) {
            case 1 -> {
                payloadValidator.validate(payload, GrpQueja.class);
                yield registrarSolicitudQueja(payload, subject);
            }
            case 2 -> {
                payloadValidator.validate(payload, GrpQueja.class);
                yield registrarSolicitudReclamo(payload, subject);
            }
            case 3 -> registrarSolicitudDenuncia(payload, subject);
            case 4 -> registrarSolicitudSujerencia(payload, subject);
            default ->
                    throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo identificare el tipo de incidence");
        };

        // Evento de registro de evidencias
        almacenarEvidencias(evidencias, incidencia);
        return incidencia;
    }

    private Integer registrarSolicitudQueja(IncidenciaPayloadCmd payload, String subject) {
        // Validaciones
        IncidenciaPayloadCmd.DetalleQuejaDto detalle = payload.getDetalleQueja();

        int vecinoId = validacionExistenciaVecino(subject);
        validarPertenenciaDomicilio(payload.getContador(), vecinoId);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        final short unidadId = detalle.getDependenciaId();
        boolean existsUnidad = catalogoRepository.existsByTableAndId((short) 6, unidadId);

        if (!existsUnidad) {
            throw new ServiceException(HttpStatus.BAD_REQUEST,
                    "No se pudo identificar la dependencia de la solicitud de queja");
        }
        // Validar UPSERT
        AsIncidencia insidencia = null;
        if (payload.getIncidenciaId() != null) {
            insidencia = incidenciaRepository.findByIdAndInVecino(payload.getIncidenciaId(), vecinoId)
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,
                            "La incidence no fue encontrada para su actualización"));
        }

        // Construcción Template
        JsonNode evidenciasJson = objectMapper.valueToTree(detalle.getTestigo());
        final AsIncidencia toPersist = IncidenciaMapper.fromDtoToQueja(
                privacidad, vecinoId, unidadId, payload, insidencia, evidenciasJson);

        // Persistencia
        AsIncidencia persisted = incidenciaRepository.save(toPersist);

        // Evento de registro de incidencia
        AsVecino vecino = vecinoRepository.findById(vecinoId).get();
        eventPublisher.publishEvent(new IncidenciaUpsertEvent(
                persisted.getId(),
                persisted.getInFecRegistro(),
                persisted.getInEstado().getDescripcion(),
                vecino.getVePersona().getFullName(),
                vecino.getVeCorreo().getCoCorreo()
        ));
        return persisted.getId();
    }

    private Integer registrarSolicitudReclamo(IncidenciaPayloadCmd payload, String subject) {
        IncidenciaPayloadCmd.DetalleReclamoDto detalle = payload.getDetalleReclamo();

        int vecinoId = validacionExistenciaVecino(subject);
        validarPertenenciaDomicilio(payload.getContador(), vecinoId);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        final short servicioId = detalle.getTipoServicioId();
        boolean existsUnidad = catalogoRepository.existsByTableAndId((short) 7, servicioId);
        if (!existsUnidad) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "Código de servicio es incorrecto");
        }
        AsIncidencia insidencia = null;
        if (payload.getIncidenciaId() != null) {
            insidencia = incidenciaRepository.findByIdAndInVecino(payload.getIncidenciaId(), vecinoId)
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,
                            "La incidence no fue encontrada para su actualización"));
        }
        final AsIncidencia toPersist = IncidenciaMapper.fromDtoToReclamo(privacidad, vecinoId, servicioId, payload, insidencia);
        AsIncidencia persisted = incidenciaRepository.save(toPersist);

        AsVecino vecino = vecinoRepository.findById(vecinoId).get();
        eventPublisher.publishEvent(new IncidenciaUpsertEvent(
                persisted.getId(),
                persisted.getInFecRegistro(),
                persisted.getInEstado().getDescripcion(),
                vecino.getVePersona().getFullName(),
                vecino.getVeCorreo().getCoCorreo()
        ));
        return persisted.getId();
    }

    private Integer registrarSolicitudDenuncia(IncidenciaPayloadCmd payload, String subject) {
        Integer vecinoId = validacionExistenciaVecino(subject);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        return null;
    }

    private Integer registrarSolicitudSujerencia(IncidenciaPayloadCmd payload, String subject) {
        Integer vecinoId = validacionExistenciaVecino(subject);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        return null;
    }

    /**
     * Verifica si el usuario existe en base de datos y obtiene el ID del vecino
     *
     * @param uuid UUID de la cuenta autenticada
     * @return ID del vecino
     */
    private Integer validacionExistenciaVecino(String uuid) {
        return usuarioRepository.findUsPersonaByUsId(uuid)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,
                        "No se pudo obtener información de la cuenta"));
    }

    /**
     * Verificar la privacidad de la solicitud
     *
     * @param pseudo Pseudónimo de privacidad
     * @return ID de privacidad en catálogo
     */
    private Short verificacionExistenciaPrivacidad(String pseudo) {
        if (pseudo.equals("PUB") || pseudo.equals("PRIV")) {
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
    private void validarPertenenciaDomicilio(String contador, int vecinoId) {
        boolean exists = vecinoDomicilioRepository.existsRelationByContadorAndVecino(contador, vecinoId);
        if (!exists) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo verificar la pertenencia de la residencia");
        }
    }

    /**
     * Almacenamiento de evidencias de incidencias
     *
     * @param evidencias   Archivos de incidencias
     * @param incidenciaId Código de incidencia
     */
    private void almacenarEvidencias(List<MultipartFile> evidencias, int incidenciaId) {
        if (evidencias != null && !evidencias.isEmpty()) {
            for (MultipartFile mf : evidencias) {
                AsArchivo archivo = fileStoragePort.storeFile(mf);

                incidenciaArchivoRepository.save(
                        AsIncidenciaArchivo.builder()
                                .id(new AsIncidenciaArchivoId(archivo.getId(), incidenciaId))
                                .aiEstado("A")
                                .build()
                );

            }
        }
    }
}
