package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.UsuarioDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.domain.repository.AsUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioQueryHandler {

    private final IdentityProviderPort identityProviderPort;
    private final AsUsuarioRepository usuarioRepository;

    public List<RoleRepresentation> getRolesPermitidos() {
        List<RoleRepresentation> roles = identityProviderPort.getRolesPermitidos();
        if (roles == null || roles.isEmpty())
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron roles permitidos");
        return roles;
    }

    public List<UsuarioDto> getUsuarios() {
        return usuarioRepository.findAll().stream().map(usuario -> {
            String role = "";
            if (usuario.getUsJsonRoles() != null && usuario.getUsJsonRoles().isArray() && !usuario.getUsJsonRoles().isEmpty()) {
                role = usuario.getUsJsonRoles().get(0).asText();
            }
            return new UsuarioDto(
                    usuario.getUsId(),
                    usuario.getUsPersona() != null ? usuario.getUsPersona().getFullName() : "N/A",
                    role,
                    usuario.getUsEstado()
            );
        }).toList();
    }
}
