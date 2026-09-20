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

    /**
     * Cierra la sesión activa en el proveedor de identidad (Keycloak) invalidando el refresh token
     *
     * @param refreshToken Token de refresco de la sesión a invalidar
     */
    void logout(String refreshToken);
}
