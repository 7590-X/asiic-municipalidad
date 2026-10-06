package com.assic.muni.application.port.out;

import com.assic.muni.application.port.out.dto.KCUsuario;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;

public interface IdentityProviderPort {

    /**
     * Crear nuevo usuario en keycloak
     *
     * @param newUser KCUsuario
     * @return ID de usuario en KC
     */
    String createNewIdentityUser(KCUsuario newUser);

    /**
     * Eliminar usuario de keycloak
     *
     * @param userId ID de usuario en KC
     */
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

    /**
     * Asignar roles a usuario en keycloak
     *
     * @param userId ID de usuario en KC
     * @param roles  Listado de roles a asignar
     */
    void asignarRoles(String userId, List<String> roles);
}
