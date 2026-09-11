package com.assic.muni.presentation.api.pubs;

import com.assic.muni.application.cqrs.cmd.ConfirmarCuentaCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.handler.ConfirmarCuentaCmdHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpResponse;
import java.time.ZonedDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/auth")
@Tag(name = "OAuth")
public class AuthController {

    private final ConfirmarCuentaCmdHandler confirmarCuentaCmdHandler;

    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión para los usuarios")
    public ResponseEntity<Object> login() {
        return null;
        // TODO: Implementar funcionalidades para inicio de sesión con credenciales registradas en Keycloak, utilizando como pasarela spring boot
    }

    @PostMapping("/logout")
    @Operation(summary = "Cierre de sesión para los usuarios")
    public ResponseEntity<Object> logout() {
        // TODO: investigar sobre cierre de sesion con keycloak, no codificar, presentar un informe técnico de alcance
        return null;
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresco de token de sesión")
    public ResponseEntity<Object> refresh() {
        return null;
        // TODO: investigar sobre refresco de token con keycloak, no codificar, presentar un informe técnico de alcance
    }


    @PostMapping("/confirmar")
    @Operation(summary = "Confirmar cuenta creada")
    public ResponseEntity<ApiResponseDto<Void>> confirmarCuentaUsuario(@RequestBody ConfirmarCuentaCmd cmd) {
        confirmarCuentaCmdHandler.handle(cmd);
        return ResponseEntity.ok().body(new ApiResponseDto<>(
                HttpStatus.OK.value(), null,
                ZonedDateTime.now(),
                "Cuenta confirmada, ahora puedes iniciar sesión en tu cuenta", null
        ));
    }
}
