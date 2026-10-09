package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.UsuarioDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.domain.model.AsUsuario;
import com.assic.muni.domain.repository.AsCatalogoRepository;
import com.assic.muni.domain.repository.AsUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioQueryHandler {

    private final IdentityProviderPort identityProviderPort;
    private final AsUsuarioRepository usuarioRepository;
    private final AsCatalogoRepository catalogoRepository;

    public List<RoleRepresentation> getRolesPermitidos() {
        List<RoleRepresentation> roles = identityProviderPort.getRolesPermitidos();
        if (roles == null || roles.isEmpty())
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron roles permitidos");
        return roles;
    }

    public List<UsuarioDto> getUsuariosInternosPorEstado(String estado) {
        short tipoUsuario = catalogoRepository.findIdByCaSeudo("RANTA")
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "No se encontró el tipo de usuario interno"));
        List<AsUsuario> usuarios = usuarioRepository.findAllByUsEstadoAndUsTipo_Id(estado, tipoUsuario);
        if (usuarios.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron usuarios internos");
        }
        return usuarios.stream()
                .map(u -> new UsuarioDto(
                        u.getUsId(),
                        u.getUsCredencial(),
                        u.getUsPersona().getFullName(),
                        u.getUsJsonRoles()))
                .collect(Collectors.toList());
    }

    public UsuarioDto getInformacionUsuario(String userId) {
        AsUsuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "No se encontró el usuario"));
        return new UsuarioDto(
                usuario.getUsId(),
                usuario.getUsCredencial(),
                usuario.getUsPersona().getFullName(),
                usuario.getUsJsonRoles());
    }
}
