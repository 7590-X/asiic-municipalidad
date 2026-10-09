package com.assic.muni.infrastructure.statemachine;

import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.model.AsIncidencia;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import static com.assic.muni.infrastructure.statemachine.StateMachineConst.H_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidenciaStateChangeInterceptor extends StateMachineInterceptorAdapter<IncidenciaState, IncidenciaEvent> {

    private final AsIncidenciaRepository incidenciaRepository;


    @Override
    public void preStateChange(State<IncidenciaState, IncidenciaEvent> state, Message<IncidenciaEvent> message, Transition<IncidenciaState, IncidenciaEvent> transition, StateMachine<IncidenciaState, IncidenciaEvent> stateMachine, StateMachine<IncidenciaState, IncidenciaEvent> rootStateMachine) {
        if (message != null && message.getHeaders().containsKey(H_ID.name())) {
            Integer incidenciaId = message.getHeaders().get(H_ID.name(), Integer.class);
            AsIncidencia incidencia = incidenciaRepository.findById(incidenciaId)
                    .orElseThrow(() -> new EntityNotFoundException("Incidencia no encontrada"));
            incidencia.setInEstado(state.getId());
            incidenciaRepository.save(incidencia);
        } else {
            log.warn("No se encontró el header con el id de la incidencia");
        }
    }
}
