package com.assic.muni.infrastructure.statemachine;

import com.assic.muni.application.port.out.IncidenciaStateMachinePort;
import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.assic.muni.infrastructure.statemachine.StateMachineConst.H_ID;
import static com.assic.muni.infrastructure.statemachine.StateMachineConst.H_BODY;

@Component
@RequiredArgsConstructor
public class IncidenciaStateMachineAdapter implements IncidenciaStateMachinePort {

    private final StateMachineFactory<IncidenciaState, IncidenciaEvent> factory;
    private final IncidenciaStateChangeInterceptor interceptor;

    @Override
    public void changeState(Integer incidenciaId, IncidenciaState currentState, IncidenciaEvent event, Object payload) {
        StateMachine<IncidenciaState, IncidenciaEvent> sm = rehidratar(incidenciaId, currentState);
        var message = MessageBuilder.withPayload(event).setHeader(H_ID.name(), incidenciaId);
        if (payload != null) {
            message.setHeader(H_BODY.name(), payload);
        }

        Message<IncidenciaEvent> msg = message.build();
        StateMachineEventResult<IncidenciaState, IncidenciaEvent> result = sm.sendEvent(Mono.just(msg)).blockLast();

        if (result == null || result.getResultType() != StateMachineEventResult.ResultType.ACCEPTED) {
            throw new IllegalStateException(
                    String.format("Transición inválida: No es posible aplicar '%s' a la incidencia %d en estado '%s'",
                            event, incidenciaId, currentState)
            );
        }
    }

    private StateMachine<IncidenciaState, IncidenciaEvent> rehidratar(Integer id, IncidenciaState currentState) {
        StateMachine<IncidenciaState, IncidenciaEvent> sm = factory.getStateMachine(id.toString());
        sm.stopReactively().block();
        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {
            accessor.addStateMachineInterceptor(interceptor);
            accessor.resetStateMachineReactively(
                    new DefaultStateMachineContext<>(currentState, null, null, null)
            ).block();
        });
        sm.startReactively().block();
        return sm;
    }
}
