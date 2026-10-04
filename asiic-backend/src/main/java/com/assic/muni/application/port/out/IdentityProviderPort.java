package com.assic.muni.application.port.out;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;

public interface IdentityProviderPort {

    String createNewIdentityUser(RegistrarVecinoCmd newUser);

    void deleteIdentityUser(String userId);

    /**
     * Confirmar correo y asignar contraseña en keycloak
     *
     * @param userId   Id de usuario en KC
     * @param password Contraseña para asignar a la cuenta en KC
     */
    void confirmIdentityUser(String userId, String password);

    /**
     * Obtener listado de roles para usuario internos del sistema
     *
     * @return Listado de roles permitidos
     */
    List<RoleRepresentation> getRolesPermitidos();
}
