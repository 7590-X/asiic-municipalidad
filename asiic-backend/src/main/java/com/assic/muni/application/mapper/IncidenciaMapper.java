package com.assic.muni.application.mapper;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd.DetalleQuejaDto;
import com.assic.muni.domain.enums.InsidenciaState;
import com.assic.muni.domain.model.AsIncidencia;
import java.util.Map;

public final class IncidenciaMapper {

    public static AsIncidencia frontDtoToQueja(short privacidad, int vecinoId, short unidadId,
                                               IncidenciaPayloadCmd payload,AsIncidencia toUpsert, Map<String,Object> evidencias) {

        if (null == toUpsert) {
            toUpsert = new AsIncidencia();
        }
        DetalleQuejaDto detalle = payload.getDetalleQueja();
        toUpsert.setId(payload.getInsidenciaId());
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
}
