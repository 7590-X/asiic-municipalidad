package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.ActualizarIncidenciaCmd;
import com.assic.muni.application.cqrs.dto.IncidenciaPayloadDto;
import com.assic.muni.application.enums.EIncidenciaEstado;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.FileStoragePort;
import com.assic.muni.domain.model.*;
import com.assic.muni.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ActualizarIncidenciaCmdHandler implements CQRSCmdHandler<String, ActualizarIncidenciaCmd> {

    private final AsInsidenciaRepository insidenciaRepository;
    private final AsCatalogoRepository catalogoRepository;
    private final AsTipoInsidenciaRepository tipoInsidenciaRepository;
    private final AsInsidenciaEstadoRepository estadoRepository;
    private final AsDenunciadoRepository denunciadoRepository;
    private final AsDomicilioRepository domicilioRepository;
    private final FileStoragePort fileStoragePort;
    private final AsInsidenciaArchivoRepository insidenciaArchivoRepository;

    @Override
    @Transactional
    public String handle(ActualizarIncidenciaCmd cmd) {
        Integer id;
        try {
            String idStr = cmd.getIncidenciaId();
            if (idStr.startsWith("ASIIC-")) {
                idStr = idStr.substring(idStr.lastIndexOf('-') + 1);
            } else if (idStr.startsWith("INC-")) {
                idStr = idStr.substring(4);
            }
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "ID de incidencia inválido");
        }

        AsInsidencia insidencia = insidenciaRepository.findById(id)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "Incidencia no encontrada"));

        if (!insidencia.getInUsrRegistro().equals(cmd.getUserId())) {
            throw new ServiceException(HttpStatus.FORBIDDEN, "No tienes permiso para actualizar esta incidencia");
        }

        if (!EIncidenciaEstado.BORRADOR.getCodigo().equals(insidencia.getInEstado().getIeId())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "Solo se pueden actualizar incidencias en estado BORRADOR");
        }

        IncidenciaPayloadDto payload = cmd.getPayload();

        // Actualizar Privacidad
        short privId = "PRIVADO".equalsIgnoreCase(payload.getPrivacidad()) ? (short) 2 : (short) 1;
        insidencia.setInPrivacidad(catalogoRepository.findById(privId)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "Privacidad inválida")));

        // Actualizar Estado
        String estadoInicial = Boolean.TRUE.equals(payload.getEsBorrador()) 
                ? EIncidenciaEstado.BORRADOR.getCodigo() 
                : EIncidenciaEstado.ENVIADA.getCodigo();
        insidencia.setInEstado(estadoRepository.findById(estadoInicial)
                .orElseThrow(() -> new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado no encontrado")));

        // Limpiar campos antes de actualizarlos
        insidencia.setInDireccion("");
        insidencia.setInComentario("");
        insidencia.setInPropuesta("");
        insidencia.setInFecInsidencia(Instant.now());

        AsCatalogo dummyCatalog = catalogoRepository.findById((short) 1).orElse(null);
        AsDomicilio dummyDomicilio = domicilioRepository.findAll().stream().findFirst().orElse(null);
        if (dummyDomicilio != null) insidencia.setInContador(dummyDomicilio);
        if (dummyCatalog != null) insidencia.setInTipoServicio(dummyCatalog);

        String tipo = payload.getTipoIncidencia().toUpperCase();
        switch (tipo) {
            case "QUEJA":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(1)).orElseThrow());
                if (payload.getDetalleQueja() != null) {
                    if (payload.getDetalleQueja().getDependenciaId() != null) {
                        insidencia.setInUnidad(catalogoRepository.findById(payload.getDetalleQueja().getDependenciaId()).orElse(null));
                    }
                    insidencia.setInEmpleado(payload.getDetalleQueja().getEmpleadoId());
                    if (payload.getDetalleQueja().getFechaIncidencia() != null) {
                        insidencia.setInFecInsidencia(payload.getDetalleQueja().getFechaIncidencia().atZone(ZoneId.systemDefault()).toInstant());
                    }
                    insidencia.setInComentario(payload.getDetalleQueja().getDescripcion() != null ? payload.getDetalleQueja().getDescripcion() : "");
                }
                break;
            case "RECLAMO":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(2)).orElseThrow());
                if (payload.getDetalleReclamo() != null) {
                    if (payload.getDetalleReclamo().getTipoServicioId() != null) {
                        insidencia.setInTipoServicio(catalogoRepository.findById(payload.getDetalleReclamo().getTipoServicioId()).orElse(dummyCatalog));
                    }
                    if (payload.getDetalleReclamo().getUbicacionGps() != null) {
                        String[] gps = payload.getDetalleReclamo().getUbicacionGps().split(",");
                        if (gps.length >= 2) {
                            insidencia.setInLatitud(gps[0].trim());
                            insidencia.setInLongitud(gps[1].trim());
                        }
                    }
                    if (payload.getDetalleReclamo().getDireccion() != null) {
                        insidencia.setInDireccion(payload.getDetalleReclamo().getDireccion());
                    }
                    if (payload.getDetalleReclamo().getNoContador() != null) {
                        domicilioRepository.findById(payload.getDetalleReclamo().getNoContador()).ifPresent(insidencia::setInContador);
                    }
                    insidencia.setInComentario(payload.getDetalleReclamo().getDescripcion() != null ? payload.getDetalleReclamo().getDescripcion() : "");
                }
                break;
            case "DENUNCIA":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(3)).orElseThrow());
                if (payload.getDetalleDenuncia() != null) {
                    if (payload.getDetalleDenuncia().getTipoDenunciaId() != null) {
                        insidencia.setInTipoDenuncia(catalogoRepository.findById(payload.getDetalleDenuncia().getTipoDenunciaId()).orElse(null));
                    }
                    if (payload.getDetalleDenuncia().getFechaHoraHechos() != null) {
                        insidencia.setInFecInsidencia(payload.getDetalleDenuncia().getFechaHoraHechos().atZone(ZoneId.systemDefault()).toInstant());
                    }
                    if (payload.getDetalleDenuncia().getDireccion() != null) {
                        insidencia.setInDireccion(payload.getDetalleDenuncia().getDireccion());
                    }
                    insidencia.setInComentario(payload.getDetalleDenuncia().getRelato() != null ? payload.getDetalleDenuncia().getRelato() : "");

                    // Denunciados update
                    if (payload.getDetalleDenuncia().getDenunciados() != null) {
                        denunciadoRepository.findByDeInsidencia(insidencia).forEach(denunciadoRepository::delete);
                        AsDenunciado denunciado = new AsDenunciado();
                        denunciado.setDeNombre(payload.getDetalleDenuncia().getDenunciados());
                        denunciado.setDeEstado("A");
                        denunciado.setDeTipo(dummyCatalog);
                        denunciado.setDeInsidencia(insidencia);
                        denunciadoRepository.save(denunciado);
                    }
                }
                break;
            case "SUGERENCIA":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(4)).orElseThrow());
                if (payload.getDetalleSugerencia() != null) {
                    if (payload.getDetalleSugerencia().getAreaId() != null) {
                        insidencia.setInArea(catalogoRepository.findById(payload.getDetalleSugerencia().getAreaId()).orElse(null));
                    }
                    insidencia.setInComentario(payload.getDetalleSugerencia().getDescripcionActual() != null ? payload.getDetalleSugerencia().getDescripcionActual() : "");
                    insidencia.setInPropuesta(payload.getDetalleSugerencia().getPropuestaMejora() != null ? payload.getDetalleSugerencia().getPropuestaMejora() : "");
                }
                break;
        }

        insidenciaRepository.save(insidencia);

        if (cmd.getEvidenciasNuevas() != null && !cmd.getEvidenciasNuevas().isEmpty()) {
            for (MultipartFile file : cmd.getEvidenciasNuevas()) {
                AsArchivo asArchivo = fileStoragePort.storeFile(file);
                AsInsidenciaArchivo ia = new AsInsidenciaArchivo();
                ia.setIaInsidencia(insidencia);
                ia.setAsArchivos(asArchivo);
                ia.setId(asArchivo.getId());
                ia.setAiFecRegistro(Instant.now());
                ia.setAiUsrRegistro(cmd.getUserId());
                ia.setAiEstado("A");
                insidenciaArchivoRepository.save(ia);
            }
        }

        return "INC-" + insidencia.getId();
    }
}
