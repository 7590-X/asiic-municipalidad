package com.assic.muni.infrastructure.service;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import com.assic.muni.application.enums.ETokenAction;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.application.port.out.TemporalTokenPort;
import com.assic.muni.infrastructure.enums.KCRole;
import com.assic.muni.infrastructure.exception.InfrastructureException;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakProviderAdapter implements IdentityProviderPort {

    private final Keycloak keycloakAdminClient;
    private final TemporalTokenPort temporalTokenPort;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public String createNewIdentityUser(RegistrarVecinoCmd newUser) {

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(newUser.getCui());
        kcUser.setEmail(newUser.getCorreo());
        kcUser.setFirstName(newUser.getNombres());
        kcUser.setLastName(newUser.getApellidos());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(false);

        try (Response response = keycloakAdminClient.realm(realm).users().create(kcUser)) {
            int status = response.getStatus();
            if (status != 201) {
                log.error("[ERROR_RESPONSE_CREAR_CUENTA_KC] {}", response.readEntity(String.class));
                throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo crear la cuenta, contactate con soporte para solucionar el problema");
            }
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf('/') + 1);
            kcAssignRole(userId, KCRole.ROLE_VECINO);
            return userId;
        } catch (RuntimeException e) {
            log.error("[ERROR_REQUEST_CREAR_CUENTA_KC]", e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un problema al intentar crear la cuenta, por favor contacte con soporte");
        }
    }

    @Override
    public void deleteIdentityUser(String userId) {
        try {
            keycloakAdminClient.realm(realm).users().get(userId).remove();
        } catch (RuntimeException e) {
            log.error("[ERROR_REQUEST_ELIMINAR_CUENTA_KC]", e);
            throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE, "Ocurrió un problema al intentar crear la cuenta, por favor contacte con soporte");
        }
    }

    @Override
    public String confirmIdentityUser(String token, String password) {
        // Validar token y obtener UUID cuenta KC
        String userId = temporalTokenPort.validateAndExtractUserId(token, ETokenAction.VERIFY_EMAIL);
        kcEmailVerified(userId, true);
        kcAssignCredencial(userId, password);

        return userId;
    }

    private void kcEmailVerified(String userId, boolean emailVerified) {
        try {
            // Validar Correo
            UserRepresentation user = keycloakAdminClient.realm(realm).users().get(userId).toRepresentation();
            user.setEmailVerified(true);
            keycloakAdminClient.realm(realm).users().get(userId).update(user);
        } catch (RuntimeException e) {
            log.error("[ERROR_REQUEST_VERIFY_EMAIL]", e);
            throw e;
        }
    }

    private void kcAssignCredencial(String userId, String password) {
        try {
            // Asignar contraseña
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            keycloakAdminClient.realm(realm).users().get(userId).resetPassword(credential);
        } catch (RuntimeException e) {
            log.error("[ERROR_REQUEST_ASSING_CREDENTIAL]", e);
            kcEmailVerified(userId, false);
            throw e;
        }
    }

    private void kcAssignRole(String userId, KCRole kcRole) {
        try {
            RoleRepresentation vecinoRole = keycloakAdminClient.realm(realm)
                    .roles()
                    .get(kcRole.getValue())
                    .toRepresentation();
            keycloakAdminClient.realm(realm).users().get(userId)
                    .roles().realmLevel().add(List.of(vecinoRole));
        } catch (RuntimeException e) {
            log.error("[ERROR_REQUEST_ASIGNAR_ROLE_KC]", e);
            deleteIdentityUser(userId);
            throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE, "Ocurrió un problema al intentar crear la cuenta, por favor contacte con soporte");
        }
    }
}
