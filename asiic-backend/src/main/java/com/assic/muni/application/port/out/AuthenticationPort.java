package com.assic.muni.application.port.out;

import com.assic.muni.application.cqrs.dto.TokenDto;

public interface AuthenticationPort {

    /**
     * Autentica a un usuario con sus credenciales ante el proveedor de identidad (Keycloak)
     *
     * @param username Nombre de usuario o correo electrónico
     * @param password Contraseña en texto plano
     * @return DTO con los tokens de acceso y refresco
     */
    TokenDto authenticate(String username, String password);
}
