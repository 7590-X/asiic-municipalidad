package com.assic.muni.presentation.api.pubs;

import com.assic.muni.application.cqrs.cmd.RegistrarVecinoCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.handler.RegistrarVecinoCmdCmdHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@RestController
@Tag(name = "Administración Pública de Vecino")
@RequestMapping("/api/v1/asiic/public/vecinos")
@RequiredArgsConstructor
public class PubVecinoController {

    private final RegistrarVecinoCmdCmdHandler registrarVecinoHandler;

    @PostMapping
    @Operation(summary = "Registro de nuevo vecino en el sistema")
    public ResponseEntity<ApiResponseDto<Void>> registrar(@Valid @RequestBody RegistrarVecinoCmd cmd) {
        registrarVecinoHandler.handle(cmd);
        return ResponseEntity.created(null)
                .body(new ApiResponseDto<>(
                        HttpStatus.CREATED.value(),
                        null,
                        ZonedDateTime.now(),
                        "Cuenta de vecino creada exitosamente, valida tu correo electrónico para validar tu cuenta",
                        null
                ));
    }
}