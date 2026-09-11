package com.assic.muni.infrastructure.listener;

import com.assic.muni.application.port.out.EmailServicePort;
import com.assic.muni.application.port.out.TemporalTokenPort;
import com.assic.muni.application.port.out.dto.SimpleMail;
import com.assic.muni.domain.event.VecinoCreadoEvent;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URI;
import java.net.URISyntaxException;
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
        variables.put("messageBody", "Ingresa al siguiente link para confirmar tu contraseña y crear una contraseña");

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
                "Portal Municipalidad - Cuenta Creada",
                html
        ));
    }

    private String obtainFrontendDomain() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isEmpty()) {
            origin = request.getHeader("Referer");
        }
        if (origin == null || origin.isEmpty()) {
            origin = request.getHeader("User-Agent");
        }
        if (origin == null || origin.isEmpty()) {
            origin = request.getHeader("X-Forwarded-For");
        }
        if (origin == null || origin.isEmpty()) {
            origin = request.getHeader("Host");
        }
        try {
            URI uri = new URI(origin);
            return uri.getHost();
        } catch (URISyntaxException e) {
            throw new InfrastructureException(HttpStatus.BAD_GATEWAY, "No se pudo identificar el origen de la solicitud");
        }
    }
}