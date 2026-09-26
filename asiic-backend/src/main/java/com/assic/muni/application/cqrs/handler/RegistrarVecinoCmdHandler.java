package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.domain.event.VecinoCreadoEvent;
import com.assic.muni.domain.model.*;
import com.assic.muni.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarVecinoCmdHandler implements CQRSCmdHandler<Integer, RegistrarVecinoCmd> {

    private final VecinoRepository vecinoRepository;
    private final AsPersonaRepository asPersonaRepository;
    private final AsCorreoRepository asCorreoRepository;
    private final AsTelefonoRepository asTelefonoRepository;
    private final DireccionRepository direccionRepository;
    private final AsCatalogoRepository catalogoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IdentityProviderPort identityProviderPort;
    private final AsDomicilioRepository domicilioRepository;
    private final AsVecinoDomicilioRepository vecinoDomicilioRepository;
    private final AsUsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Integer handle(RegistrarVecinoCmd cmd) {

        int validacion = asPersonaRepository.validateByPeCuiAndPeCoCorreo(cmd.getCui(), cmd.getCorreo());
        if (validacion != 0) {
            switch (validacion) {
                case 1:
                    throw new ServiceException(HttpStatus.CONFLICT, "El número de CUI no se encuentra disponible");
                case 2:
                    throw new ServiceException(HttpStatus.CONFLICT, "El correo no se encuentra disponible");
                default:
                    throw new RuntimeException("No se pudo validar la entrada de datos.");
            }
        }

        String userId = identityProviderPort.createNewIdentityUser(cmd);

        try {
            AsCatalogo tipoPersona = catalogoRepository.findByCaSeudo("PEIN")
                    .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "No se pudo verificar la identidad del vecino"));

            String ip = (cmd.getIpRegistro() != null && !cmd.getIpRegistro().isBlank())
                    ? cmd.getIpRegistro()
                    : "127.0.0.1";

            AsPersona persona = asPersonaRepository.save(AsPersona.builder()
                    .peCui(cmd.getCui())
                    .peNit(cmd.getNit())
                    .pePasaporte(cmd.getPasaporte())
                    .peNombre(cmd.getNombres())
                    .peApellido(cmd.getApellidos())
                    .peGenero(cmd.getGenero().toUpperCase())
                    .peEstadoCivil(cmd.getEstadoCivilId())
                    .peTipPersona(tipoPersona.getId())
                    .build());

            AsCorreo correo = asCorreoRepository.save(AsCorreo.builder()
                    .coCorreo(cmd.getCorreo())
                    .coUsrRegistro(userId)
                    .build());

            AsTelefono telefono = asTelefonoRepository.save(AsTelefono.builder()
                    .teTelefono(cmd.getTelefono().trim())
                    .teUsrRegistro(userId)
                    .build());

            AsDireccion direccion = direccionRepository.save(AsDireccion.builder()
                    .diDireccion(cmd.getDireccion())
                    .diLocacion(cmd.getLocacionId())
                    .build());

            AsVecino vecino = vecinoRepository.save(AsVecino.builder()
                    .vePersona(persona)
                    .veCorreo(correo)
                    .veTelefono(telefono)
                    .veProfesion(cmd.getProfesionId())
                    .veEstado("A")
                    .veUsrRegistro(userId)
                    .veIpRegistro(ip)
                    .build());

            AsDomicilio domicilio = domicilioRepository.save(AsDomicilio.builder()
                    .doContador(cmd.getNoContador())
                    .doDireccion(direccion)
                    .build());

            vecinoDomicilioRepository.save(AsVecinoDomicilio.builder()
                    .vdDomicilio(domicilio)
                    .vdVecino(vecino)
                    .vdEstado("A")
                    .build());

            AsCatalogo tipoUsuario = catalogoRepository.findByCaSeudo("RVECO")
                    .orElseThrow(() -> new ServiceException(HttpStatus.NOT_IMPLEMENTED, "No se pudo determinar el tipo de usuario en el sistema"));

            usuarioRepository.save(AsUsuario.builder()
                    .usId(userId)
                    .usTipo(tipoUsuario)
                    .usEstado("I") // Se activa hasta que se confirma la cuenta
                    .usIpRegistro(ip)
                    .usPersona(persona)
                    .usUsrRegistro(userId)
                    .build());
            log.info("[ACCOUNT_CREATED] Cuenta de usuario creado con UUID:{}, ID Vecino: {}", userId, vecino.getId());
        } catch (RuntimeException e) {
            log.error("[REGISTRO_VECINO_ERROR] Error al registrar entidades del vecino. Compensando usuario en Identity Provider...", e);
            identityProviderPort.deleteIdentityUser(userId);
            throw e;
        }
        eventPublisher.publishEvent(new VecinoCreadoEvent(
                userId, cmd.getCorreo(), (cmd.getNombres() + " " + cmd.getApellidos())
        ));
        return null;
    }
}
