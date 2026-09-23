package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.IncidenciaDetalleDto;
import com.assic.muni.application.cqrs.query.GetIncidenciaByIdQuery;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.model.AsInsidencia;
import com.assic.muni.domain.model.AsDenunciado;
import com.assic.muni.domain.repository.AsInsidenciaRepository;
import com.assic.muni.domain.repository.AsDenunciadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetIncidenciaByIdQueryHandler implements CQRSCmdHandler<IncidenciaDetalleDto, GetIncidenciaByIdQuery> {

    private final AsInsidenciaRepository insidenciaRepository;
    private final AsDenunciadoRepository denunciadoRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    @Transactional(readOnly = true)
    public IncidenciaDetalleDto handle(GetIncidenciaByIdQuery query) {
        Integer id;
        try {
            String idStr = query.getIncidenciaId();
            if (idStr.startsWith("ASIIC-")) {
                idStr = idStr.substring(idStr.lastIndexOf('-') + 1);
            } else if (idStr.startsWith("INC-")) {
                idStr = idStr.substring(4);
            }
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "ID de incidencia inválido");
        }

        AsInsidencia entity = insidenciaRepository.findById(id)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "Incidencia no encontrada"));

        // Verificar pertenencia
        if (!entity.getInUsrRegistro().equals(query.getUserId())) {
            throw new ServiceException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta incidencia");
        }

        return mapToDto(entity);
    }

    private IncidenciaDetalleDto mapToDto(AsInsidencia entity) {
        String tipo = entity.getInTipoInsidencia() != null ? entity.getInTipoInsidencia().getTiNombre().toUpperCase() : "";
        String estado = entity.getInEstado() != null ? entity.getInEstado().getIeId() : "";

        String formattedId = "INC-" + entity.getId();
        if (entity.getInFecRegistro() != null) {
            formattedId = "ASIIC-" + entity.getInFecRegistro().atZone(ZoneId.systemDefault()).getYear() + "-" + String.format("%04d", entity.getId());
        }

        IncidenciaDetalleDto.IncidenciaDetalleDtoBuilder builder = IncidenciaDetalleDto.builder()
                .id(formattedId)
                .tipoIncidencia(tipo)
                .estado(estado)
                .privacidad(entity.getInPrivacidad() != null && entity.getInPrivacidad().getId() == 2 ? "PRIVADO" : "PUBLICO")
                .direccion(entity.getInDireccion())
                .descripcion(entity.getInComentario());

        switch (tipo) {
            case "QUEJA":
                builder.dependenciaId(entity.getInUnidad() != null ? entity.getInUnidad().getId() : null);
                builder.empleadoId(entity.getInEmpleado());
                builder.fechaIncidencia(entity.getInFecInsidencia() != null ? formatter.format(entity.getInFecInsidencia()) : null);
                builder.lugar(entity.getInDireccion()); // Usamos direccion como lugar
                break;
            case "RECLAMO":
                builder.tipoServicioId(entity.getInTipoServicio() != null ? entity.getInTipoServicio().getId() : null);
                builder.ubicacionGps((entity.getInLatitud() != null ? entity.getInLatitud() : "") + "," + (entity.getInLongitud() != null ? entity.getInLongitud() : ""));
                builder.noContador(entity.getInContador() != null ? entity.getInContador().getDoContador() : null);
                break;
            case "DENUNCIA":
                builder.tipoDenunciaId(entity.getInTipoDenuncia() != null ? entity.getInTipoDenuncia().getId() : null);
                builder.fechaHoraHechos(entity.getInFecInsidencia() != null ? formatter.format(entity.getInFecInsidencia()) : null);
                builder.relato(entity.getInComentario());
                
                // Fetch denunciados
                List<AsDenunciado> denunciadosList = denunciadoRepository.findByDeInsidencia(entity);
                if (denunciadosList != null && !denunciadosList.isEmpty()) {
                    builder.denunciados(denunciadosList.get(0).getDeNombre());
                }
                break;
            case "SUGERENCIA":
                builder.areaId(entity.getInArea() != null ? entity.getInArea().getId() : null);
                builder.descripcionActual(entity.getInComentario());
                builder.propuestaMejora(entity.getInPropuesta());
                break;
        }

        return builder.build();
    }
}
