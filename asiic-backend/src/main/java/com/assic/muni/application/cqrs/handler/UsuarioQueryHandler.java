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

import com.assic.muni.domain.repository.AsMuniUsuarioRepository;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioQueryHandler {

    private final IdentityProviderPort identityProviderPort;
    private final AsUsuarioRepository usuarioRepository;
    private final AsMuniUsuarioRepository muniUsuarioRepository;

    public List<RoleRepresentation> getRolesPermitidos() {
        List<RoleRepresentation> roles = identityProviderPort.getRolesPermitidos();
        if (roles == null || roles.isEmpty())
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron roles permitidos");
        return roles;
    }

    public List<UsuarioDto> getUsuarios() {
        Map<String, List<Short>> munisPorUsuario = muniUsuarioRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        mu -> mu.getId().getMuUsuario(),
                        Collectors.mapping(mu -> mu.getId().getMuMuni(), Collectors.toList())
                ));

        return usuarioRepository.findAll().stream().map(usuario -> {
            List<String> roles = new ArrayList<>();
            if (usuario.getUsJsonRoles() != null && usuario.getUsJsonRoles().isArray()) {
                usuario.getUsJsonRoles().forEach(node -> roles.add(node.asText()));
            }
            if (usuario.getUsTipo() != null && usuario.getUsTipo().getCaSeudo() != null) {
                if (!roles.contains(usuario.getUsTipo().getCaSeudo())) {
                    roles.add(usuario.getUsTipo().getCaSeudo());
                }
            }
            
            List<Short> municipalidades = munisPorUsuario.getOrDefault(usuario.getUsId(), new ArrayList<>());

            return new UsuarioDto(
                    usuario.getUsId(),
                    usuario.getUsPersona() != null ? usuario.getUsPersona().getFullName() : "N/A",
                    roles,
                    municipalidades,
                    usuario.getUsEstado()
            );
        }).toList();
    }
}
