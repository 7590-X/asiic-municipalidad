package com.assic.muni.application.mapper;

import java.util.Collections;
import java.util.List;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd.DetalleQuejaDto;
import com.assic.muni.application.cqrs.dto.*;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.model.AsIncidencia;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class IncidenciaMapper {

    public static AsIncidencia fromDtoToQueja(short privacidad, int vecinoId, short unidadId, IncidenciaPayloadCmd payload, AsIncidencia toUpsert, JsonNode evidencias) {
        DetalleQuejaDto detalle = payload.getDetalleQueja();
        AsIncidencia incidencia = mapearCamposComunes(privacidad, vecinoId, payload, toUpsert);
        incidencia.setInDireccion(detalle.getDireccionReferencial());
        incidencia.setInUnidad(unidadId);
        incidencia.setInEmpleado(detalle.getNombreEmpleado().trim().toUpperCase());
        incidencia.setInFecInsidencia(detalle.getFechaIncidencia());
        incidencia.setInComentario(detalle.getDescripcion());
        incidencia.setInLatitud(detalle.getLatitudGps());
        incidencia.setInLongitud(detalle.getLongitudGps());
        incidencia.setInJsons(evidencias);
        return incidencia;
    }

    public static AsIncidencia fromDtoToReclamo(short privacidad, int vecinoId, short servicio, IncidenciaPayloadCmd payload, AsIncidencia toUpsert) {
        IncidenciaPayloadCmd.DetalleReclamoDto detalle = payload.getDetalleReclamo();
        AsIncidencia incidencia = mapearCamposComunes(privacidad, vecinoId, payload, toUpsert);
        incidencia.setInTipoServicio(servicio);
        incidencia.setInComentario(detalle.getDescripcion());
        incidencia.setInLatitud(detalle.getLatitudGps());
        incidencia.setInLongitud(detalle.getLongitudGps());
        return incidencia;
    }

    public static AsIncidencia fromDtoToDenuncia(short privacidad, int vecinoId, short tipoDenuncia, IncidenciaPayloadCmd payload, AsIncidencia toUpsert, JsonNode denunciados) {
        IncidenciaPayloadCmd.DetalleDenunciaDto detalle = payload.getDetalleDenuncia();
        AsIncidencia incidencia = mapearCamposComunes(privacidad, vecinoId, payload, toUpsert);
        incidencia.setInTipoDenuncia(tipoDenuncia);
        incidencia.setInFecInsidencia(detalle.getFechaHoraHechos());
        incidencia.setInComentario(detalle.getRelato());
        incidencia.setInLatitud(detalle.getLatitudGps());
        incidencia.setInLongitud(detalle.getLongitudGps());
        incidencia.setInJsons(denunciados);
        return incidencia;
    }

    public static AsIncidencia fromDtoToSugerencia(short privacidad, int vecinoId, short area, IncidenciaPayloadCmd payload, AsIncidencia toUpsert) {
        IncidenciaPayloadCmd.DetalleSugerenciaDto detalle = payload.getDetalleSugerencia();
        AsIncidencia incidencia = mapearCamposComunes(privacidad, vecinoId, payload, toUpsert);
        incidencia.setInArea(area);
        incidencia.setInComentario(detalle.getDescripcionActual());
        incidencia.setInPropuesta(detalle.getPropuestaMejora());
        return incidencia;
    }

    /**
     * Mapea los campos comunes a todos los tipos de incidencia en estado de borrador
     *
     * @param privacidad ID de privacidad en catálogo
     * @param vecinoId   Código de vecino
     * @param payload    Payload de la incidencia
     * @param toUpsert   Incidencia existente a actualizar, o null para un nuevo registro
     * @return Incidencia con los campos comunes asignados
     */
    private static AsIncidencia mapearCamposComunes(short privacidad, int vecinoId, IncidenciaPayloadCmd payload, AsIncidencia toUpsert) {
        AsIncidencia incidencia = toUpsert != null ? toUpsert : new AsIncidencia();
        incidencia.setId(payload.getIncidenciaId());
        incidencia.setInTipoIncidencia(payload.getTipoIncidencia());
        incidencia.setInPrivacidad(privacidad);
        incidencia.setInVecino(vecinoId);
        incidencia.setInContador(payload.getContador());
        incidencia.setInEstado(IncidenciaState.BORRADOR);
        return incidencia;
    }

    public static IncidenciaDto entityToIncidenciaDto(AsIncidencia entity, ObjectMapper objectMapper) {
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
                testigos = objectMapper.convertValue(entity.getInJsons(), new TypeReference<>() {
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
                .estado(entity.getInEstado().getDescripcion())
                .testigos(testigos)
                .fechaRegistro(entity.getInFecRegistro())
                .fechaModifico(entity.getInFecModifico())
                .build();
    }
}