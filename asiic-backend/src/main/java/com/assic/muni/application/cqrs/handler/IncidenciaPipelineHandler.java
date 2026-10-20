package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.dto.IncidenciaDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.mapper.IncidenciaMapper;
import com.assic.muni.application.port.out.IncidenciaStateMachinePort;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.model.AsIncidencia;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaPipelineHandler {

    private final AsIncidenciaRepository incidenciaRepository;
    private final IncidenciaStateMachinePort incidenciaStateMachinePort;
    private final UpsertIncidenciaCmdHandler upsertIncidenciaCmdHandler;
    private final ObjectMapper objectMapper;

    public void pipelineChangeState(Integer incidenciaId, IncidenciaEvent event) {
        final IncidenciaState current = getEstadoById(incidenciaId);
        incidenciaStateMachinePort.changeState(incidenciaId, current, event, null);
    }

    public void pipelineChangeState(Integer incidenciaId, IncidenciaEvent event, Object payload) {
        final IncidenciaState current = getEstadoById(incidenciaId);
        incidenciaStateMachinePort.changeState(incidenciaId, current, event, payload);
    }

    private IncidenciaState getEstadoById(int incidenciaId) {
        return incidenciaRepository.findStateById(incidenciaId)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND,
                        "No se pudo obtener información de la incidencia"));
    }

    public List<IncidenciaDto> obtenerIncidenciasAsignadasByEstado(IncidenciaState... estados) {
        final String usuarioId = JwtExtractor.extrarJwtSubject();
        List<AsIncidencia> list = incidenciaRepository.findByUsuarioAsignadoAndInEstadoIn(usuarioId, List.of(estados));
        if (list.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron incidencias asignadas");
        }
        return list.stream()
                .map((i) -> IncidenciaMapper.entityToIncidenciaDto(i, objectMapper))
                .toList();
    }

    @Transactional
    public void pipelineApelarIncidencia(int incidenciaId, IncidenciaPayloadCmd payload,
            List<MultipartFile> evidencias) {
        if (incidenciaId != payload.getIncidenciaId()) {
            throw new ServiceException(HttpStatus.BAD_REQUEST,
                    "El id de la incidencia no coincide con el id del payload");
        }
        upsertIncidenciaCmdHandler.handle(payload);
        pipelineChangeState(payload.getIncidenciaId(), IncidenciaEvent.APELAR_INCIDENCIA);
    }
}
