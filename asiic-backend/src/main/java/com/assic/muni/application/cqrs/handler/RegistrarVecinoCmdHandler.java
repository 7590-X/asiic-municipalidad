package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import org.springframework.stereotype.Service;

@Service
public class RegistrarVecinoCmdHandler implements CQRSHandler<String, RegistrarVecinoCmd> {

    @Override
    public String handle(RegistrarVecinoCmd cmd) {
        return "";
    }
}
