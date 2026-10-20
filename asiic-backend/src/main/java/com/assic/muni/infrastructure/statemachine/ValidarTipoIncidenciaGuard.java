package com.assic.muni.infrastructure.statemachine;

import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

/**
 * Guard que verifica si el tipo de incidencia tiene permitido omitir la recolección
 * de información de parte de un agente de campo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidarTipoIncidenciaGuard implements Guard<IncidenciaState, IncidenciaEvent> {

    private final AsIncidenciaRepository incidenciaRepository;
    private static final short TIPO_INCIDENCIA_PERMITIDA = 4;

    @Override
    public boolean evaluate(StateContext<IncidenciaState, IncidenciaEvent> context) {
        final int id = context.getMessageHeaders().get(StateMachineConst.H_ID.name(), Integer.class);
        final boolean permitida = incidenciaRepository.existsByIdAndInTipoIncidencia(id, TIPO_INCIDENCIA_PERMITIDA);
        if (permitida)
            return true;
        log.warn("Incidencia con id {} no tiene permitido omitir la recolección de información de parte de un agente de campo", id);
        return false;
    }
}
