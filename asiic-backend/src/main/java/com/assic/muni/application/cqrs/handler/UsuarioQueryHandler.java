package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IdentityProviderPort;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioQueryHandler {

    private final IdentityProviderPort identityProviderPort;

    public List<RoleRepresentation> getRolesPermitidos() {
        List<RoleRepresentation> roles = identityProviderPort.getRolesPermitidos();
        if (roles == null || roles.isEmpty())
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron roles permitidos");
        return roles;
    }
}
