package com.assic.muni.application.port.out;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;

public interface IdentityProviderPort {

    String createNewIdentityUser(RegistrarVecinoCmd newUser);

    void deleteIdentityUser(String userId);

    /**
     * Confirmar correo y asignar contraseña en keycloak
     *
     * @param token    Token que contiene UUID de cuenta KC
     * @param password Contraseña para asignar a la cuenta en KC
     * @return Retornar UUID de cuenta KC
     */
    String confirmIdentityUser(String token, String password);
}
