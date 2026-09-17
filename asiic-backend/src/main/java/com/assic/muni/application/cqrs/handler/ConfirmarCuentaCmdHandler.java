package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.ConfirmarCuentaCmd;
import com.assic.muni.application.enums.ETokenAction;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.application.port.out.TemporalTokenPort;
import com.assic.muni.domain.event.CuentaConfirmadaEvent;
import com.assic.muni.domain.model.AsPersona;
import com.assic.muni.domain.model.AsUsuario;
import com.assic.muni.domain.repository.AsUsuarioRepository;
import com.assic.muni.domain.repository.AsVecinoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmarCuentaCmdHandler implements CQRSVoidCmdHandler<ConfirmarCuentaCmd> {

    private final IdentityProviderPort identityProviderPort;
    private final TemporalTokenPort temporalTokenPort;
    private final ApplicationEventPublisher eventPublisher;

    private final AsVecinoRepository vecinoRepository;
    private final AsUsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void handle(ConfirmarCuentaCmd cmd) {
        String userId = temporalTokenPort.validateAndExtractUserId(cmd.token(), ETokenAction.VERIFY_EMAIL);
        AsUsuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,
                        "No se pudo encontrar información de la cuenta"));
        if (usuario.getUsEstado().equalsIgnoreCase("A")) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "La cuenta ya ha sido confirmada anteriormente");
        }
        identityProviderPort.confirmIdentityUser(userId, cmd.password());
        usuario.setUsEstado("A");
        usuarioRepository.save(usuario);

        // Obtener nombre completo
        final AsPersona persona = usuario.getUsPersona();
        final String fullName = persona.getPeNombre() + " " + persona.getPeApellido();

        // Obtener correo
        String email = vecinoRepository.findEmailByVeId(persona.getId()).orElseThrow(
                () -> new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo verificar el correo electrónico"));

        // Publicar evento
        eventPublisher.publishEvent(new CuentaConfirmadaEvent(fullName, usuario.getUsFecModifico(), email));
    }
}
