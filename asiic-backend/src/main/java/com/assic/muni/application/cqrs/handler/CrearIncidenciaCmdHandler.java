package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.CrearIncidenciaCmd;
import com.assic.muni.application.cqrs.dto.IncidenciaPayloadDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.enums.EIncidenciaEstado;
import com.assic.muni.domain.model.*;
import com.assic.muni.domain.repository.*;
import com.assic.muni.application.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrearIncidenciaCmdHandler implements CQRSCmdHandler<String, CrearIncidenciaCmd> {

    private final VecinoRepository vecinoRepository;
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
    public String handle(CrearIncidenciaCmd cmd) {
        IncidenciaPayloadDto payload = cmd.getPayload();

        AsVecino vecino = vecinoRepository.findByVeUsrRegistro(cmd.getUserId())
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "No se encontró el perfil del vecino"));

        AsInsidencia insidencia = new AsInsidencia();
        insidencia.setInVecino(vecino);
        insidencia.setInUsrRegistro(cmd.getUserId());
        insidencia.setInFecRegistro(Instant.now());

        // Privacidad
        short privId = "PRIVADO".equalsIgnoreCase(payload.getPrivacidad()) ? (short) 2 : (short) 1;
        insidencia.setInPrivacidad(catalogoRepository.findById(privId)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "Privacidad inválida")));

        // Estado inicial
        String estadoInicial = Boolean.TRUE.equals(payload.getEsBorrador()) 
                ? EIncidenciaEstado.BORRADOR.getCodigo() 
                : EIncidenciaEstado.ENVIADA.getCodigo();
        insidencia.setInEstado(estadoRepository.findById(estadoInicial)
                .orElseThrow(
                        () -> new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado inicial no encontrado")));

        // Dummy values for required fields that might not be filled by all forms:
        insidencia.setInDireccion("");
        insidencia.setInComentario("");
        insidencia.setInPropuesta("");
        insidencia.setInFecInsidencia(Instant.now());

        // Dummy references for NOT NULL fields that are context specific
        // We will default to id 1 if nothing is provided just to satisfy NOT NULL
        // constraints if they aren't filled.
        // It's better to fetch a default if they are required in schema but not in
        // form.
        AsCatalogo dummyCatalog = catalogoRepository.findById((short) 1).orElse(null);
        AsDomicilio dummyDomicilio = domicilioRepository.findAll().stream().findFirst().orElse(null);

        if (dummyDomicilio != null)
            insidencia.setInContador(dummyDomicilio);
        if (dummyCatalog != null)
            insidencia.setInTipoServicio(dummyCatalog);

        String tipo = payload.getTipoIncidencia().toUpperCase();
        switch (tipo) {
            case "QUEJA":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(1))
                        .orElseThrow(() -> new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Tipo Queja no encontrado")));

                if (payload.getDetalleQueja() != null) {
                    if (payload.getDetalleQueja().getDependenciaId() != null) {
                        insidencia.setInUnidad(
                                catalogoRepository.findById(payload.getDetalleQueja().getDependenciaId()).orElse(null));
                    }
                    insidencia.setInEmpleado(payload.getDetalleQueja().getEmpleadoId());
                    if (payload.getDetalleQueja().getFechaIncidencia() != null) {
                        insidencia.setInFecInsidencia(payload.getDetalleQueja().getFechaIncidencia()
                                .atZone(ZoneId.systemDefault()).toInstant());
                    }
                    insidencia.setInComentario(payload.getDetalleQueja().getDescripcion() != null
                            ? payload.getDetalleQueja().getDescripcion()
                            : "");
                }
                break;

            case "RECLAMO":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(2))
                        .orElseThrow(() -> new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Tipo Reclamo no encontrado")));

                if (payload.getDetalleReclamo() != null) {
                    if (payload.getDetalleReclamo().getTipoServicioId() != null) {
                        insidencia.setInTipoServicio(catalogoRepository
                                .findById(payload.getDetalleReclamo().getTipoServicioId()).orElse(dummyCatalog));
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
                        domicilioRepository.findById(payload.getDetalleReclamo().getNoContador())
                                .ifPresent(insidencia::setInContador);
                    }
                    insidencia.setInComentario(payload.getDetalleReclamo().getDescripcion() != null
                            ? payload.getDetalleReclamo().getDescripcion()
                            : "");
                }
                break;

            case "DENUNCIA":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(3))
                        .orElseThrow(() -> new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Tipo Denuncia no encontrado")));

                if (payload.getDetalleDenuncia() != null) {
                    if (payload.getDetalleDenuncia().getTipoDenunciaId() != null) {
                        insidencia.setInTipoDenuncia(catalogoRepository
                                .findById(payload.getDetalleDenuncia().getTipoDenunciaId()).orElse(null));
                    }
                    if (payload.getDetalleDenuncia().getFechaHoraHechos() != null) {
                        insidencia.setInFecInsidencia(payload.getDetalleDenuncia().getFechaHoraHechos()
                                .atZone(ZoneId.systemDefault()).toInstant());
                    }
                    if (payload.getDetalleDenuncia().getDireccion() != null) {
                        insidencia.setInDireccion(payload.getDetalleDenuncia().getDireccion());
                    }
                    insidencia.setInComentario(
                            payload.getDetalleDenuncia().getRelato() != null ? payload.getDetalleDenuncia().getRelato()
                                    : "");
                }
                break;

            case "SUGERENCIA":
                insidencia.setInTipoInsidencia(tipoInsidenciaRepository.findById(BigDecimal.valueOf(4))
                        .orElseThrow(() -> new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Tipo Sugerencia no encontrado")));

                if (payload.getDetalleSugerencia() != null) {
                    if (payload.getDetalleSugerencia().getAreaId() != null) {
                        insidencia.setInArea(
                                catalogoRepository.findById(payload.getDetalleSugerencia().getAreaId()).orElse(null));
                    }
                    insidencia.setInComentario(payload.getDetalleSugerencia().getDescripcionActual() != null
                            ? payload.getDetalleSugerencia().getDescripcionActual()
                            : "");
                    insidencia.setInPropuesta(payload.getDetalleSugerencia().getPropuestaMejora() != null
                            ? payload.getDetalleSugerencia().getPropuestaMejora()
                            : "");
                }
                break;

            default:
                throw new ServiceException(HttpStatus.BAD_REQUEST, "Tipo de incidencia desconocido: " + tipo);
        }

        insidencia = insidenciaRepository.save(insidencia);

        // Guardar Denunciados si es Denuncia
        if ("DENUNCIA".equals(tipo) && payload.getDetalleDenuncia() != null
                && payload.getDetalleDenuncia().getDenunciados() != null) {
            AsDenunciado denunciado = new AsDenunciado();
            denunciado.setDeNombre(payload.getDetalleDenuncia().getDenunciados());
            denunciado.setDeEstado("A");
            denunciado.setDeTipo(dummyCatalog); // TODO: requires actual catalog if defined
            denunciado.setDeInsidencia(insidencia);
            denunciadoRepository.save(denunciado);
        }

        // Manejo de archivos
        if (cmd.getEvidencias() != null && !cmd.getEvidencias().isEmpty()) {
            for (MultipartFile file : cmd.getEvidencias()) {
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

        return "INC-" + insidencia.getId(); // Return a tracking code
    }
}
