package com.assic.muni.infrastructure.statemachine;

import com.assic.muni.application.cqrs.dto.ResumenDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.model.AsResumen;
import com.assic.muni.domain.model.AsResumenId;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import com.assic.muni.domain.repository.AsResumenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumenRecoleccionAction implements Action<IncidenciaState, IncidenciaEvent> {

    private final AsIncidenciaRepository incidenciaRepository;
    private final AsResumenRepository resumenRepository;

    @Override
    @Transactional
    public void execute(StateContext<IncidenciaState, IncidenciaEvent> context) {
        final String userId = context.getMessageHeaders().get(StateMachineConst.H_USER.name(), String.class);
        final int id = context.getMessageHeaders().get(StateMachineConst.H_ID.name(), Integer.class);

        log.info("Ejecutando resumen para incidencia {}, agente de campo {}", id, userId);

        final ResumenDto resumen = context.getMessageHeaders().get(StateMachineConst.H_BODY.name(), ResumenDto.class);

        final boolean pertenece = incidenciaRepository.existsByIdAndInAgente(id, userId);
        if (!pertenece) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "La incidencia no pertenece al agente de campo");
        }
        String textoSeguro = Jsoup.clean(resumen.body(), Safelist.basic());
        int version = resumenRepository.findCountByIdReIncidencia(id).orElse(0) + 1;
        AsResumen r = AsResumen.builder()
                .id(new AsResumenId(id, (short) version))
                .reBody(textoSeguro)
                .reLatitud(resumen.latitudGps())
                .reLongitud(resumen.longitudGps())
                .build();
        resumenRepository.save(r);
        log.info("Resumen guardado con éxito, version {}, incidencia, {}. agente {}", version, id, userId);
    }
}
