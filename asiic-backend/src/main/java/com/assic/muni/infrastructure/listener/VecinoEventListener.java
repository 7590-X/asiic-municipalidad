package com.assic.muni.infrastructure.listener;

import com.assic.muni.application.port.out.EmailServicePort;
import com.assic.muni.domain.event.VecinoCreadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VecinoEventListener {

    private final EmailServicePort emailServicePort;

    @EventListener
    public void eventVecinoCreado(VecinoCreadoEvent event){

    }

}
