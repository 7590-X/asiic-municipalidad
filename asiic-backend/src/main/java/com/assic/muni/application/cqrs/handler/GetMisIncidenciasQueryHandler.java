package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.GetMisIncidenciasQuery;
import com.assic.muni.application.cqrs.dto.IncidenciaResumenDto;
import com.assic.muni.application.enums.EIncidenciaEstado;
import com.assic.muni.domain.model.AsInsidencia;
import com.assic.muni.domain.repository.AsInsidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetMisIncidenciasQueryHandler implements CQRSCmdHandler<List<IncidenciaResumenDto>, GetMisIncidenciasQuery> {

    private final AsInsidenciaRepository insidenciaRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResumenDto> handle(GetMisIncidenciasQuery query) {
        List<AsInsidencia> incidencias = insidenciaRepository.findByInUsrRegistroOrderByInFecRegistroDesc(query.getUserId());

        return incidencias.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private IncidenciaResumenDto mapToDto(AsInsidencia entity) {
        String tipo = entity.getInTipoInsidencia() != null ? entity.getInTipoInsidencia().getTiNombre() : "Desconocido";
        String estado = entity.getInEstado() != null ? entity.getInEstado().getIeNombre() : EIncidenciaEstado.BORRADOR.getNombre();
        
        String dependencia = "No Asignado";
        if (entity.getInUnidad() != null) {
            dependencia = entity.getInUnidad().getCaValor();
        } else if (entity.getInTipoServicio() != null && "Reclamo".equalsIgnoreCase(tipo)) {
            dependencia = entity.getInTipoServicio().getCaValor();
        } else if (entity.getInArea() != null && "Sugerencia".equalsIgnoreCase(tipo)) {
            dependencia = entity.getInArea().getCaValor();
        }

        String asunto = entity.getInComentario();
        if (asunto == null || asunto.trim().isEmpty()) {
            asunto = entity.getInPropuesta();
        }
        if (asunto == null || asunto.trim().isEmpty()) {
            asunto = "Sin detalle";
        }

        String fecha = "";
        if (entity.getInFecInsidencia() != null) {
            fecha = formatter.format(entity.getInFecInsidencia());
        } else if (entity.getInFecRegistro() != null) {
            fecha = formatter.format(entity.getInFecRegistro());
        }

        return IncidenciaResumenDto.builder()
                .id("ASIIC-" + entity.getInFecRegistro().atZone(ZoneId.systemDefault()).getYear() + "-" + String.format("%04d", entity.getId()))
                .tipo(tipo)
                .asunto(asunto)
                .dependencia(dependencia)
                .fecha(fecha)
                .estado(estado)
                .build();
    }
}
