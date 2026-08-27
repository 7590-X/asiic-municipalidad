package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.TokenDto;
import org.springframework.stereotype.Service;

@Service
public class LoginCmdHandler implements CQRSHandler<TokenDto, LoginCmdHandler> {

    @Override
    public TokenDto handle(LoginCmdHandler cmd) {
        return null;
    }
}
