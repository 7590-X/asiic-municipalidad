package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.RegistrarUsuarioCmd;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.application.port.out.dto.KCUsuario;
import com.assic.muni.domain.event.CuentaCreadaEvent;
import com.assic.muni.domain.model.*;
import com.assic.muni.domain.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class RegistrarUsuarioCmdHandler implements CQRSCmdHandler<String, RegistrarUsuarioCmd> {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AsUsuarioRepository usuarioRepository;
    private final AsMuniUsuarioRepository muniUsuarioRepository;
    private final AsPersonaRepository personaRepository;
    private final IdentityProviderPort identityProviderPort;
    private final ApplicationEventPublisher eventPublisher;
    private final AsCatalogoRepository catalogoRepository;
    private final AsCorreoRepository correoRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public String handle(RegistrarUsuarioCmd cmd) {

        boolean existsCorreo = usuarioRepository.existsByUsCorreo_CoCorreo(cmd.correo());
        if (existsCorreo) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "El correo ya fue registrado para otro usuario");
        }

        final String username = generarCodigoAleatorio(10);
        String userId = identityProviderPort.createNewIdentityUser(new KCUsuario(
                username, cmd.correo(), cmd.nombres(), cmd.apellidos(), false, true
        ));
        try {
            Short tipoPersona = catalogoRepository.findIdByCaSeudo("PEIN") // Persona Individual
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo verificar la identidad del vecino"));

            AsPersona persona = personaRepository.save(AsPersona.builder()
                    .peNombre(cmd.nombres())
                    .peApellido(cmd.apellidos())
                    .peTipPersona(tipoPersona)
                    .build());

            AsCatalogo tipoUsuario = catalogoRepository.findByCaSeudo("RANTA") // Usuario Interno
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo verificar el tipo de usuario"));

            JsonNode jsonRoles = objectMapper.valueToTree(cmd.roles());

            AsCorreo correo = correoRepository.saveAndFlush(AsCorreo.builder()
                    .coCorreo(cmd.correo())
                    .build());

            usuarioRepository.saveAndFlush(AsUsuario.builder()
                    .usId(userId)
                    .usEstado("I")
                    .usCorreo(correo)
                    .usTipo(tipoUsuario)
                    .usPersona(persona)
                    .usUsrRegistro(userId)
                    .usIpRegistro("N/A")
                    .usCredencial(username)
                    .usJsonRoles(jsonRoles)
                    .build());

            muniUsuarioRepository.saveAll(cmd.municipalidades().stream()
                    .map(m -> AsMuniUsuario.builder()
                            .id(new AsMuniUsuarioId(m, userId))
                            .build()).toList());

            identityProviderPort.asignarRoles(userId, cmd.roles());
        } catch (RuntimeException e) {
            identityProviderPort.deleteIdentityUser(userId);
            throw e;
        }

        eventPublisher.publishEvent(new CuentaCreadaEvent(
                userId, cmd.correo(), (cmd.nombres() + " " + cmd.apellidos())
        ));

        return userId;
    }

    private String generarCodigoAleatorio(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return sb.toString().toUpperCase();
    }
}
