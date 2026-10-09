package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IncidenciaStateMachinePort;
import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncidenciaPipelineHandler {

    private final AsIncidenciaRepository incidenciaRepository;
    private final IncidenciaStateMachinePort incidenciaStateMachinePort;

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
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "No se pudo obtener información de la incidencia"));
    }
}
