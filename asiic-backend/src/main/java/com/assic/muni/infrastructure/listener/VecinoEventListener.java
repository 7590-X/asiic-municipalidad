package com.assic.muni.infrastructure.listener;

import com.assic.muni.application.port.out.EmailServicePort;
import com.assic.muni.application.port.out.TemporalTokenPort;
import com.assic.muni.application.port.out.dto.SimpleMail;
import com.assic.muni.domain.event.CuentaConfirmadaEvent;
import com.assic.muni.domain.event.VecinoCreadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VecinoEventListener {

    private final EmailServicePort emailServicePort;
    private final TemporalTokenPort temporalTokenPort;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.domain}")
    private String frontendDomain;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void eventVecinoCreado(VecinoCreadoEvent event) {

        Map<String, Object> variables = new HashMap<>();

        variables.put("title", "Cuenta de Vecino Creada Exitosamente");
        variables.put("messageBody", "Confirmar tu cuenta y crear una contraseña");

        final String token = temporalTokenPort.generateVerifyEmailToken(event.userId());

        final String link = frontendDomain + "/confirmar-cuenta?token=" + token;

        variables.put("buttonUrl", link);
        variables.put("buttonText", "Confirmar");
        variables.put("footerText", "Municipalidades de Guatemala");

        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process("correo-template", context);

        emailServicePort.sendSimpleEmail(new SimpleMail(
                event.email(),
                "ASIIC Municipalidades - Cuenta Creada",
                html));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void eventCuentaConfirmada(CuentaConfirmadaEvent event) {
        Map<String, Object> variables = new HashMap<>();

        variables.put("title", "Cuenta de Vecino Confirmada Exitosamente");
        variables.put("messageBody",
                String.format("""
                        Estimado vecino %s, tu cuenta ha sido confirmada a las %s exitosamente,
                        ahora puedes acceder a la plataforma de manera ilimitada para hacer tus gestiones.""",
                        event.fullName(), event.confirmationTime().toString()));

        variables.put("footerText", "Municipalidades de Guatemala");

        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process("correo-template", context);

        emailServicePort.sendSimpleEmail(new SimpleMail(
                event.email(),
                "ASIIC Municipalidades - Cuenta Confirmada",
                html));
    }
}