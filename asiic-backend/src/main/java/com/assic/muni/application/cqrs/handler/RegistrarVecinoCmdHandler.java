package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.model.*;
import com.assic.muni.infrastructure.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.context.ApplicationEventPublisher;
import com.assic.muni.domain.event.VecinoCreadoEvent;
import java.time.ZonedDateTime;
import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarVecinoCmdHandler implements CQRSHandler<URI, RegistrarVecinoCmd> {

    private final VecinoRepository vecinoRepository;
    private final PersonaRepository personaRepository;
    private final CorreoRepository correoRepository;
    private final TelefonoRepository telefonoRepository;
    private final DireccionRepository direccionRepository;
    private final AsCatalogoRepository catalogoRepository;
    private final LocacionRepository locacionRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final Keycloak keycloakAdminClient;

    @Value("${keycloak.realm}")
    private String realm;

    private static final String T_TIPO_PERSONA = "as_tipo_persona";
    private static final String T_PROFESION = "as_profesion";
    private static final String T_ESTADO_CIVIL = "as_estado_civil";

    // RN1/RN2: el vecino es persona individual; el sistema lo da de alta como Activo.
    private static final String VAL_TIPO_PERSONA_VECINO = "Individual";
    private static final String ESTADO_ACTIVO = "A";
    private static final String ROL_VECINO = "ROLE_VECINO";

    @Override
    @Transactional
    public URI handle(RegistrarVecinoCmd cmd) {

        // Validación de CUI y Correo Electrónico
        int validacion = personaRepository.validateByPeCuiAndPeCoCorreo(cmd.getCui(), cmd.getCorreo());
        if (validacion != 0) {
            switch (validacion) {
                case 1:
                case 2:
                    throw new ServiceException(HttpStatus.CONFLICT,
                            "El CUI o Correo Ingresado ya se encuentra registrado en el sistema");
                default:
                    throw new RuntimeException("No se pudo validar la entrada de datos.");
            }
        }

        AsCatalogo tipoPersona = catalogoRepository.findByCaSeudo("PEIN")
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,"No se pudo verificar la identidad del vecino"));

        // 3. Contraseña temporal segura (paso 2.3.6)
        String passwordTemporal = generarPasswordTemporal();

        // 4. Alta en Keycloak
        URI location = crearUsuarioKeycloak(cmd, passwordTemporal);
        String keycloakUserId = extraerId(location);

        try {
            // 5. Rol ROLE_VECINO
            RoleRepresentation vecinoRole = keycloakAdminClient.realm(realm)
                    .roles().get(ROL_VECINO).toRepresentation();
            keycloakAdminClient.realm(realm).users().get(keycloakUserId)
                    .roles().realmLevel().add(List.of(vecinoRole));

            // 6. Persistencia del alta
            Instant ahora = Instant.now();
            String ip = obtenerIpCliente();

            AsPersona persona = personaRepository.save(AsPersona.builder()
                    .peCui(cmd.getCui())
                    .peNit(cmd.getNit())
                    .pePasaporte(cmd.getPasaporte())
                    .peNombre(cmd.getNombres())
                    .peApellido(cmd.getApellidos())
                    .peGenero(cmd.getGenero().toUpperCase())
                    .peEstadoCivil(cmd.getEstadoCivilId())
                    .peTipPersona(tipoPersona.getId())
                    .build());

            AsCorreo correo = correoRepository.save(AsCorreo.builder()
                    .coCorreo(cmd.getCorreo())
                    .coFecRegistro(ahora)
                    .coUsrRegistro(cmd.getCui())
                    .build());

            AsTelefono telefono = telefonoRepository.save(AsTelefono.builder()
                    .teTelefono(cmd.getTelefono().trim())
                    .teFecRegistro(ahora)
                    .teUsrRegistro(cmd.getCui())
                    .build());

            AsDireccione direccion = direccionRepository.save(AsDireccione.builder()
                    .diDireccion(cmd.getDireccion())
                    .diLocacion(cmd.getLocacionId())
                    .diFecRegistro(ahora)
                    .build());

            vecinoRepository.save(AsVecino.builder()
                    .vePersona(persona)          // @MapsId -> ve_id = persona.getId()
                    .veCorreo(correo)
                    .veTelefono(telefono)
                    .veProfesion(cmd.getProfesionId())
                    .veEstado(ESTADO_ACTIVO)
                    .veFecRegistro(ahora)
                    .veIpRegistro(ip)
                    .build());

            eventPublisher.publishEvent(new VecinoCreadoEvent(
                    cmd.getCorreo(),
                    passwordTemporal,
                    "Cuenta de vecino creada exitosamente",
                    ZonedDateTime.now()
            ));

            // TODO fuera de alcance: FA3 (correo de bienvenida con contraseña temporal)
            //                        y bitácora de auditoría (paso 9).
            return location;

        } catch (RuntimeException ex) {
            eliminarUsuarioKeycloakSilencioso(keycloakUserId); // @Transactional revierte BD, no Keycloak
            throw ex;
        }
    }

    // ---------- Keycloak ----------

    private URI crearUsuarioKeycloak(RegistrarVecinoCmd cmd, String passwordTemporal) {
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(cmd.getCui());
        kcUser.setEmail(cmd.getCorreo());
        kcUser.setFirstName(cmd.getNombres());
        kcUser.setLastName(cmd.getApellidos());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(passwordTemporal);
        credential.setTemporary(true);
        kcUser.setCredentials(List.of(credential));

        try (Response response = keycloakAdminClient.realm(realm).users().create(kcUser)) {
            int status = response.getStatus();
            if (status == 409) {
                throw new ServiceException(HttpStatus.CONFLICT,
                        "El CUI o Correo Ingresado ya se encuentra registrado en el sistema");
            }
            if (status != 201) {
                throw new ServiceException(HttpStatus.BAD_GATEWAY,
                        "No fue posible crear la cuenta en el proveedor de identidad (HTTP " + status + ")");
            }
            return response.getLocation();
        }
    }

    private void eliminarUsuarioKeycloakSilencioso(String keycloakUserId) {
        try {
            keycloakAdminClient.realm(realm).users().get(keycloakUserId).remove();
            log.warn("Usuario {} revertido en Keycloak tras fallo de persistencia.", keycloakUserId);
        } catch (RuntimeException e) {
            log.error("No se pudo revertir el usuario {} en Keycloak. Requiere limpieza manual.", keycloakUserId, e);
        }
    }

    private String extraerId(URI location) {
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    // ---------- Utilidades ----------

    private String obtenerIpCliente() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "127.0.0.1";
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }

    private String generarPasswordTemporal() {
        byte[] randomBytes = new byte[9];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}