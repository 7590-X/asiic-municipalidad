package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import com.assic.muni.application.cqrs.handler.RegistrarVecinoCmdHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/asiic/public/vecinos")
@RequiredArgsConstructor
public class VecinoController {

    private final RegistrarVecinoCmdHandler registrarVecinoHandler;

    @PostMapping("")
    public ResponseEntity<String> registrar(@Valid @RequestBody RegistrarVecinoCmd cmd) {
        URI keycloakResourceUri = registrarVecinoHandler.handle(cmd);
        return ResponseEntity.created(keycloakResourceUri).body("Cuenta de usuario creada exitosamente");
    }
}