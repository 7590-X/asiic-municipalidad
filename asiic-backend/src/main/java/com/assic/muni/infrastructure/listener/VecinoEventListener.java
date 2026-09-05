package com.assic.muni.infrastructure.listener;

import com.assic.muni.application.port.out.EmailServicePort;
import com.assic.muni.application.port.out.dto.SimpleMail;
import com.assic.muni.domain.event.VecinoCreadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VecinoEventListener {

    private final EmailServicePort emailServicePort;

    // Se dispara SOLO si la transacción del registro hizo COMMIT (FA3: la cuenta queda creada).
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void eventVecinoCreado(VecinoCreadoEvent event) {

        String cuerpo = """
                Bienvenido al Portal Municipal.

                Su cuenta de vecino ha sido creada exitosamente.

                Contraseña temporal: %s

                Ingrese al portal con su CUI y esta contraseña; el sistema le pedirá
                cambiarla en su primer inicio de sesión.

                Si no puede ingresar, utilice la opción "Recuperar contraseña".
                """.formatted(event.tempPassword());

        emailServicePort.sendSimpleEmail(new SimpleMail(
                event.email(),
                "Bienvenido al Portal Municipal - Credenciales de acceso",
                cuerpo
        ));
        log.info("Correo de bienvenida encolado para {}", event.email());
    }
}