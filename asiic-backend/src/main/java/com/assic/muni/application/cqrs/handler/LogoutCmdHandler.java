package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.LogoutCmd;
import com.assic.muni.application.port.out.AuthenticationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCmdHandler implements CQRSCmdHandler<Void, LogoutCmd> {

    private final AuthenticationPort authenticationPort;

    @Override
    public Void handle(LogoutCmd cmd) {
        log.info("[LOGOUT_REQUEST] Procesando cierre de sesión");
        authenticationPort.logout(cmd.refreshToken());
        return null;
    }
}
