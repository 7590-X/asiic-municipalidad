package com.assic.muni.infrastructure.listener;

import com.assic.muni.application.port.out.EmailServicePort;
import com.assic.muni.application.port.out.TemporalTokenPort;
import com.assic.muni.application.port.out.dto.SimpleMail;
import com.assic.muni.domain.event.CuentaConfirmadaEvent;
import com.assic.muni.domain.event.VecinoCreadoEvent;
import com.assic.muni.infrastructure.util.ConstantsString;
import com.assic.muni.infrastructure.util.CorreoVariablesBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class VecinoEventListener {

    private final EmailServicePort emailServicePort;
    private final TemporalTokenPort temporalTokenPort;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.domain}")
    private String frontendDomain;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void eventVecinoCreado(VecinoCreadoEvent event) {
        final String token = temporalTokenPort.generateVerifyEmailToken(event.userId());
        final String link = frontendDomain + "/confirmar-cuenta?token=" + token;

        String html = CorreoVariablesBuilder.builder()
                .title("Cuenta de Vecino Creada Exitosamente")
                .messageBody("Confirmar tu cuenta y crear una contraseña")
                .buttonUrl(link)
                .buttonText("Confirmar")
                .processWith(templateEngine);

        emailServicePort.sendSimpleEmail(new SimpleMail(
                event.email(),
                ConstantsString.getEmailSubjectBase("Cuenta Creada"),
                html));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void eventCuentaConfirmada(CuentaConfirmadaEvent event) {
        String html = CorreoVariablesBuilder.builder()
                .title("Cuenta de Vecino Confirmada Exitosamente")
                .messageBody(String.format("""
                                Estimado vecino %s, tu cuenta ha sido confirmada a las %s exitosamente,
                                ahora puedes acceder a la plataforma de manera ilimitada para hacer tus gestiones.""",
                        event.fullName(), event.confirmationTime().toString()))
                .processWith(templateEngine);
        emailServicePort.sendSimpleEmail(new SimpleMail(
                event.email(),
                ConstantsString.getEmailSubjectBase("Cuenta Confirmada"),
                html));
    }
}