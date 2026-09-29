package com.assic.muni.application.cqrs.handler;

import java.util.List;

import com.assic.muni.application.group.GrpDenuncia;
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

    // Tablas de catálogo
    private static final short CATALOGO_DEPENDENCIA = 6;
    private static final short CATALOGO_TIPO_SERVICIO = 7;
    private static final short CATALOGO_TIPO_DENUNCIA = 8;

    /**
     * Construcción de la entidad a persistir según el tipo de incidencia
     */
    @FunctionalInterface
    private interface IncidenciaBuilder {
        AsIncidencia build(short privacidad, int vecinoId, AsIncidencia existente);
    }

    // Repositorios
    private final AsUsuarioRepository usuarioRepository;
    private final AsCatalogoRepository catalogoRepository;
    private final AsVecinoDomicilioRepository vecinoDomicilioRepository;
    private final AsIncidenciaRepository incidenciaRepository;
    private final AsIncidenciaArchivoRepository incidenciaArchivoRepository;
    private final VecinoRepository vecinoRepository;

    // Puertos
    private final FileStoragePort fileStoragePort;

    // Eventos
    private final ApplicationEventPublisher eventPublisher;

    // Utils
    private final PayloadValidator payloadValidator;
    private final ObjectMapper objectMapper;

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
            case 3 -> {
                payloadValidator.validate(payload, GrpDenuncia.class);
                yield registrarSolicitudDenuncia(payload, subject);
            }

            case 4 -> registrarSolicitudSugerencia(payload, subject);
            default ->
                    throw new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo identificare el tipo de incidence");
        };

        // Evento de registro de evidencias
        almacenarEvidencias(evidencias, incidencia);
        return incidencia;
    }

    private Integer registrarSolicitudQueja(IncidenciaPayloadCmd payload, String subject) {
        IncidenciaPayloadCmd.DetalleQuejaDto detalle = payload.getDetalleQueja();
        final short unidadId = detalle.getDependenciaId();
        JsonNode testigos = objectMapper.valueToTree(detalle.getTestigo());

        return registrarIncidencia(payload, subject,
                CATALOGO_DEPENDENCIA, unidadId,
                "No se pudo identificar la dependencia de la solicitud de queja",
                "La queja no fue encontrada para su actualización",
                (privacidad, vecinoId, existente) -> IncidenciaMapper.fromDtoToQueja(
                        privacidad, vecinoId, unidadId, payload, existente, testigos));
    }

    private Integer registrarSolicitudReclamo(IncidenciaPayloadCmd payload, String subject) {
        final short servicioId = payload.getDetalleReclamo().getTipoServicioId();

        return registrarIncidencia(payload, subject,
                CATALOGO_TIPO_SERVICIO, servicioId,
                "Código de servicio es incorrecto",
                "El reclamo no fue encontrado para su actualización",
                (privacidad, vecinoId, existente) -> IncidenciaMapper.fromDtoToReclamo(
                        privacidad, vecinoId, servicioId, payload, existente));
    }

    private Integer registrarSolicitudDenuncia(IncidenciaPayloadCmd payload, String subject) {
        IncidenciaPayloadCmd.DetalleDenunciaDto detalle = payload.getDetalleDenuncia();
        final short tipoDenuncia = detalle.getTipoDenunciaId();
        JsonNode denunciados = objectMapper.valueToTree(detalle.getDenunciados());

        return registrarIncidencia(payload, subject,
                CATALOGO_TIPO_DENUNCIA, tipoDenuncia,
                "Código de denuncia es incorrecto",
                "La denuncia no fue encontrada para su actualización",
                (privacidad, vecinoId, existente) -> IncidenciaMapper.fromDtoToDenuncia(
                        privacidad, vecinoId, tipoDenuncia, payload, existente, denunciados));
    }

    private Integer registrarSolicitudSugerencia(IncidenciaPayloadCmd payload, String subject) {
        Integer vecinoId = validacionExistenciaVecino(subject);
        Short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        return null;
    }

    /**
     * Flujo común de UPSERT de una incidencia: validaciones, construcción,
     * persistencia y publicación del evento de registro
     *
     * @param payload             Payload de la incidencia
     * @param subject             UUID de la cuenta autenticada
     * @param tablaCatalogo       Tabla de catálogo del tipo específico de incidencia
     * @param catalogoId          ID del registro de catálogo a validar
     * @param msgCatalogoInvalido Mensaje de error si el catálogo no existe
     * @param msgNoEncontrada     Mensaje de error si la incidencia a actualizar no existe
     * @param builder             Construcción de la entidad según el tipo de incidencia
     * @return ID de la incidencia persistida
     */
    private Integer registrarIncidencia(IncidenciaPayloadCmd payload, String subject,
                                        short tablaCatalogo, short catalogoId,
                                        String msgCatalogoInvalido, String msgNoEncontrada,
                                        IncidenciaBuilder builder) {
        // Validaciones
        int vecinoId = validacionExistenciaVecino(subject);
        validarPertenenciaDomicilio(payload.getContador(), vecinoId);
        short privacidad = verificacionExistenciaPrivacidad(payload.getPrivacidad());
        validarExistenciaCatalogo(tablaCatalogo, catalogoId, msgCatalogoInvalido);

        // Validar UPSERT
        AsIncidencia existente = obtenerIncidenciaExistente(payload.getIncidenciaId(), vecinoId, msgNoEncontrada);

        // Construcción y persistencia
        AsIncidencia persisted = incidenciaRepository.save(builder.build(privacidad, vecinoId, existente));

        // Evento de registro de incidencia
        publicarEventoUpsert(persisted, vecinoId);
        return persisted.getId();
    }

    /**
     * Valida que exista el registro en la tabla de catálogo indicada
     */
    private void validarExistenciaCatalogo(short tabla, short id, String mensajeError) {
        if (!catalogoRepository.existsByTableAndId(tabla, id)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, mensajeError);
        }
    }

    /**
     * Obtiene la incidencia a actualizar del vecino, o null si es un nuevo registro
     */
    private AsIncidencia obtenerIncidenciaExistente(Integer incidenciaId, int vecinoId, String mensajeError) {
        if (incidenciaId == null) {
            return null;
        }
        return incidenciaRepository.findByIdAndInVecino(incidenciaId, vecinoId)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, mensajeError));
    }

    /**
     * Publica el evento de registro/actualización de la incidencia
     */
    private void publicarEventoUpsert(AsIncidencia persisted, int vecinoId) {
        AsVecino vecino = vecinoRepository.findById(vecinoId)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND,
                        "No se encontró información del vecino"));
        eventPublisher.publishEvent(new IncidenciaUpsertEvent(
                persisted.getId(),
                persisted.getInFecRegistro(),
                persisted.getInEstado().getDescripcion(),
                vecino.getVePersona().getFullName(),
                vecino.getVeCorreo().getCoCorreo()
        ));
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
