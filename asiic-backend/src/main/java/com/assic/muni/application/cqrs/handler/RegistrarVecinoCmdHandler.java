package com.assic.muni.application.cqrs.handler;

import org.springframework.stereotype.Service;

@Service
public class RegistrarVecinoCmdHandler implements CQRSHandler<String, RegistrarVecinoCmdHandler> {

    @Override
    public String handle(RegistrarVecinoCmdHandler cmd) {
        return "";
    }
}
