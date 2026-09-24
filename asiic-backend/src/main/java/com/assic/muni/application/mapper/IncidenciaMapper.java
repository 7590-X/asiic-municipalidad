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


public final class IncidenciaMapper {

    public static AsIncidencia frontDtoToQueja(short privacidad, int vecinoId, short unidadId,
                                               IncidenciaPayloadCmd payload, AsIncidencia toUpsert,
                                               JsonNode evidencias) {

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
        if (entity == null) {
            return null;
        }

        TipoIncidenciaDto tipoIncidencia = null;
        if (entity.getInTipoIncidenciaObj() != null) {
            tipoIncidencia = new TipoIncidenciaDto(
                    entity.getInTipoIncidenciaObj().getId(),
                    entity.getInTipoIncidenciaObj().getTiNombre(),
                    entity.getInTipoIncidenciaObj().getTiDescripcion()
            );
        }

        CatalogoItemDto privacidad = null;
        if (entity.getInPrivacidadObj() != null) {
            privacidad = new CatalogoItemDto(
                    entity.getInPrivacidadObj().getId(),
                    entity.getInPrivacidadObj().getCaValor(),
                    entity.getInPrivacidadObj().getCaSeudo()
            );
        }

        CatalogoItemDto dependencia = null;
        if (entity.getInUnidadObj() != null) {
            dependencia = new CatalogoItemDto(
                    entity.getInUnidadObj().getId(),
                    entity.getInUnidadObj().getCaValor(),
                    entity.getInUnidadObj().getCaSeudo()
            );
        }

        VecinoDto vecino = null;
        if (entity.getInVecinoObj() != null) {
            var v = entity.getInVecinoObj();
            String cui = null;
            String nombreCompleto = null;
            if (v.getVePersona() != null) {
                cui = v.getVePersona().getPeCui();
                nombreCompleto = (v.getVePersona().getPeNombre() + " " + v.getVePersona().getPeApellido()).trim();
            }
            String correo = v.getVeCorreo() != null ? v.getVeCorreo().getCoCorreo() : null;
            String telefono = v.getVeTelefono() != null ? v.getVeTelefono().getTeTelefono() : null;
            vecino = new VecinoDto(v.getId(), cui, nombreCompleto, correo, telefono);
        }

        DomicilioDto domicilio = null;
        if (entity.getInContadorObj() != null) {
            var d = entity.getInContadorObj();
            String direccion = d.getDoDireccion() != null ? d.getDoDireccion().getDiDireccion() : null;
            domicilio = new DomicilioDto(d.getDoContador(), direccion, d.getDoLatitud(), d.getDoLongitud());
        }

        List<TestigoResumenDto> testigos = Collections.emptyList();
        if (entity.getInJsons() != null && !entity.getInJsons().isNull() && !entity.getInJsons().isEmpty()) {
            try {
                testigos = objectMapper.convertValue(
                        entity.getInJsons(),
                        new TypeReference<List<TestigoResumenDto>>() {
                        }
                );
            } catch (Exception ignored) {
                testigos = Collections.emptyList();
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
                .usuarioRegistro(entity.getInUsrRegistro())
                .fechaModifico(entity.getInFecModifico())
                .usuarioModifico(entity.getInUsrModifico())
                .build();
    }
}
