package com.assic.muni.application.port.out;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;

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
}
