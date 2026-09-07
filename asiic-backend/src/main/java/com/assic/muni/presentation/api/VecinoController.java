package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import com.assic.muni.application.cqrs.handler.RegistrarVecinoCmdCmdHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Tag(name = "Administración Pública de Vecino")
@RequestMapping("/api/v1/asiic/public/vecinos")
@RequiredArgsConstructor
public class VecinoController {

    private final RegistrarVecinoCmdCmdHandler registrarVecinoHandler;

    @PostMapping
    @Operation(summary = "Registro de nuevo vecino en el sistema")
    public ResponseEntity<String> registrar(@Valid @RequestBody RegistrarVecinoCmd cmd) {
        URI keycloakResourceUri = registrarVecinoHandler.handle(cmd);
        return ResponseEntity.created(keycloakResourceUri).body("Cuenta de usuario creada exitosamente");
    }
}