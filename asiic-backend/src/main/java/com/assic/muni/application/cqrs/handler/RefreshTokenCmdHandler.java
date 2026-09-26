package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.SessionTokenCmd;
import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.application.port.out.AuthenticationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCmdHandler implements CQRSCmdHandler<TokenDto, SessionTokenCmd> {

    private final AuthenticationPort authenticationPort;

    @Override
    public TokenDto handle(SessionTokenCmd cmd) {
        log.info("[REFRESH_TOKEN_REQUEST] Procesando solicitud de refresco de token");
        return authenticationPort.refreshToken(cmd.refreshToken());
    }
}
