package com.assic.muni.application.mapper;

import java.util.Collections;
import java.util.List;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd.DetalleQuejaDto;
import com.assic.muni.application.cqrs.dto.*;
import com.assic.muni.domain.enums.InsidenciaState;
import com.assic.muni.domain.model.AsIncidencia;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class IncidenciaMapper {

    public static AsIncidencia frontDtoToQueja(short privacidad, int vecinoId, short unidadId, IncidenciaPayloadCmd payload, AsIncidencia toUpsert, JsonNode evidencias) {

        if (null == toUpsert) {
            toUpsert = new AsIncidencia();
        }
        DetalleQuejaDto detalle = payload.getDetalleQueja();
        toUpsert.setId(payload.getIncidenciaId());
        toUpsert.setInTipoIncidencia(payload.getTipoIncidencia());
        toUpsert.setInPrivacidad(privacidad);
        toUpsert.setInVecino(vecinoId);
        toUpsert.setInContador(payload.getContador());
        toUpsert.setInDireccion(detalle.getDireccionReferencial());
        toUpsert.setInUnidad(unidadId);
        toUpsert.setInEmpleado(detalle.getNombreEmpleado().trim().toUpperCase());
        toUpsert.setInFecInsidencia(detalle.getFechaIncidencia());
        toUpsert.setInComentario(detalle.getDescripcion());
        toUpsert.setInLatitud(detalle.getLatitudGps());
        toUpsert.setInLongitud(detalle.getLongitudGps());
        toUpsert.setInEstado(InsidenciaState.BORRADOR.name());
        toUpsert.setInJsons(evidencias);
        return toUpsert;
    }

    public static IncidenciaDto entityToQuejaDto(AsIncidencia entity, ObjectMapper objectMapper) {
        // Mandatorio en todas las solicitudes
        TipoIncidenciaDto tipoIncidencia = TipoIncidenciaMapper.fronEntityToDto(entity.getInTipoIncidenciaObj());
        CatalogoItemDto privacidad = CatalogoMapper.fromEntityToDto(entity.getInPrivacidadObj());

        CatalogoItemDto dependencia = null;
        if (entity.getInUnidadObj() != null) {
            dependencia = CatalogoMapper.fromEntityToDto(entity.getInUnidadObj());
        }
        VecinoDto vecino = VecinoMapper.fronEntityToDto(entity.getInVecinoObj());
        DomicilioDto domicilio = DomicilioMapper.frontEntityToDto(entity.getInContadorObj());

        List<TestigoDto> testigos = Collections.emptyList();
        if (entity.getInJsons() != null && !entity.getInJsons().isNull() && !entity.getInJsons().isEmpty()) {
            try {
                testigos = objectMapper.convertValue(entity.getInJsons(), new TypeReference<List<TestigoDto>>() {
                });
            } catch (Exception ignored) {
                log.error("[JSON_MAPPER] No se pudo convertir testigos", ignored);
            }
        }
        return IncidenciaDto.builder()
                .id(entity.getId())
                .tipoIncidencia(tipoIncidencia)
                .privacidad(privacidad)
                .vecino(vecino)
                .domicilio(domicilio)
                .dependencia(dependencia)
                .direccionReferencial(entity.getInDireccion())
                .nombreEmpleado(entity.getInEmpleado())
                .fechaIncidencia(entity.getInFecInsidencia())
                .descripcion(entity.getInComentario())
                .latitud(entity.getInLatitud())
                .longitud(entity.getInLongitud())
                .estado(entity.getInEstado())
                .testigos(testigos)
                .fechaRegistro(entity.getInFecRegistro())
                .fechaModifico(entity.getInFecModifico())
                .build();
    }
}