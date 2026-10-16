package com.assic.muni.infrastructure.statemachine;

import com.assic.muni.application.cqrs.dto.IncidenciaFeedback;
import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.model.AsRechazo;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import com.assic.muni.domain.repository.AsRechazoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RechazarIncidenciaAction implements Action<IncidenciaState, IncidenciaEvent> {

    private final AsIncidenciaRepository incidenciaRepository;
    private final AsRechazoRepository rechazoRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void execute(StateContext<IncidenciaState, IncidenciaEvent> context) {
        final int incidenciaId = context.getMessageHeaders().get(StateMachineConst.H_ID.name(), Integer.class);
        try {
            log.info("Procesando rechazo de incidencia {}", incidenciaId);
            List<IncidenciaFeedback> feedbacks = context.getMessageHeaders().get(StateMachineConst.H_BODY.name(), List.class);
            incidenciaRepository.actualizarCantidadRechazos(incidenciaId);
            short cantidadRechazos = incidenciaRepository.findInRechazosById(incidenciaId).orElse((short) 0);

            AsRechazo rechazo = AsRechazo.builder()
                    .reIncidencia(incidenciaId)
                    .reNumero(cantidadRechazos)
                    .reDatos(objectMapper.valueToTree(feedbacks))
                    .build();

            rechazoRepository.save(rechazo);
            log.info("Rechazo de incidencia {} procesado exitosamente", incidenciaId);
        } catch (Exception e) {
            log.error("Error al procesar rechazo de incidencia {}", incidenciaId, e);
            throw e;
        }
    }
}
