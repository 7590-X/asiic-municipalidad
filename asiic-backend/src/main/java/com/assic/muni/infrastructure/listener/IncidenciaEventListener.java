package com.assic.muni.infrastructure.listener;

import com.assic.muni.application.port.out.EmailServicePort;
import com.assic.muni.application.port.out.dto.SimpleMail;
import com.assic.muni.domain.event.IncidenciaUpsertEvent;
import com.assic.muni.infrastructure.util.ConstantsString;
import com.assic.muni.infrastructure.util.CorreoVariablesBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidenciaEventListener {

    private final EmailServicePort emailServicePort;
    private final TemplateEngine templateEngine;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIncidenciaUpsert(IncidenciaUpsertEvent event) {

        String html = CorreoVariablesBuilder.builder()
                .title("Incidencia #" + event.id() + " Registrada Exitosamente")
                .messageBody("Se ha registrado una nueva incidencia vecino " + event.vecino() + " con el estado " + event.estado())
                .processWith(templateEngine);

        emailServicePort.sendSimpleEmail(new SimpleMail(
                event.email(),
                ConstantsString.getEmailSubjectBase("Incidencia Registrada"),
                html));
    }

}
