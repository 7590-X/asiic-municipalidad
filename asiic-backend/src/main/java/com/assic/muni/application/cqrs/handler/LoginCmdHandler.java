package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.LoginCmd;
import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.application.port.out.AuthenticationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCmdHandler implements CQRSCmdHandler<TokenDto, LoginCmd> {

    private final AuthenticationPort authenticationPort;

    @Override
    public TokenDto handle(LoginCmd cmd) {
        log.info("[LOGIN_REQUEST] Iniciando proceso de autenticación para el usuario: {}", cmd.username());
        return authenticationPort.authenticate(cmd.username(), cmd.password());
    }
}
