package com.assic.muni.application.port.out;

import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;

public interface IncidenciaStateMachinePort {
    void changeState(Integer incidenciaId, IncidenciaState currentState, IncidenciaEvent event, Object payload);
}
