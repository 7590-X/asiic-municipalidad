package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.ConfirmarCuentaCmd;
import com.assic.muni.application.port.out.IdentityProviderPort;
import com.assic.muni.infrastructure.repository.AsUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmarCuentaCmdHandler implements CQRSCmdHandler<Void, ConfirmarCuentaCmd>{

    private final AsUsuarioRepository usuarioRepository;
    private final IdentityProviderPort identityProviderPort;

    @Override
    @Transactional
    public Void handle(ConfirmarCuentaCmd cmd) {
        identityProviderPort.confirmIdentityUser(cmd.token(),cmd.password());
    }
}
