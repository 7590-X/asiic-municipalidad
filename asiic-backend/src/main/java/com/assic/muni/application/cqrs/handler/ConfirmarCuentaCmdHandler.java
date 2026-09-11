package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.ConfirmarCuentaCmd;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.domain.model.AsUsuario;
import com.assic.muni.infrastructure.repository.AsUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmarCuentaCmdHandler implements CQRSVoidCmdHandler<ConfirmarCuentaCmd>{

    private final AsUsuarioRepository usuarioRepository;
    private final IdentityProviderPort identityProviderPort;

    @Override
    @Transactional
    public void handle(ConfirmarCuentaCmd cmd) {
        String userId = identityProviderPort.confirmIdentityUser(cmd.token(),cmd.password());
        AsUsuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,"No se pudo encontrar información de la cuenta"));
        usuario.setUsEstado("A");
        usuarioRepository.save(usuario);
    }
}
