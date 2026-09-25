package com.assic.muni.presentation.api.pubs;

import com.assic.muni.application.cqrs.cmd.ConfirmarCuentaCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.handler.ConfirmarCuentaCmdHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import java.time.ZonedDateTime;

import com.assic.muni.application.cqrs.cmd.LoginCmd;
import com.assic.muni.application.cqrs.cmd.SessionTokenCmd;
import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.application.cqrs.handler.LoginCmdHandler;
import com.assic.muni.application.cqrs.handler.LogoutCmdHandler;
import com.assic.muni.application.cqrs.handler.RefreshTokenCmdHandler;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/auth")
@Tag(name = "Autenticación", description = "Gestión de seguridad de usuarios")
public class AuthController {

    private final ConfirmarCuentaCmdHandler confirmarCuentaCmdHandler;
    private final LoginCmdHandler loginCmdHandler;
    private final LogoutCmdHandler logoutCmdHandler;
    private final RefreshTokenCmdHandler refreshTokenCmdHandler;

    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión para los usuarios")
    public ResponseEntity<ApiResponseDto<TokenDto>> login(@Valid @RequestBody LoginCmd cmd) {
        TokenDto token = loginCmdHandler.handle(cmd);
        return ResponseEntity.ok(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "/api/v1/asiic/auth/login",
                ZonedDateTime.now(),
                "Inicio de sesión exitoso",
                token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cierre de sesión para los usuarios")
    public ResponseEntity<ApiResponseDto<Void>> logout(@Valid @RequestBody SessionTokenCmd cmd) {
        logoutCmdHandler.handle(cmd);
        return ResponseEntity.ok(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "/api/v1/asiic/auth/logout",
                ZonedDateTime.now(),
                "Sesión cerrada exitosamente",
                null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresco de token de sesión")
    public ResponseEntity<ApiResponseDto<TokenDto>> refresh(@Valid @RequestBody SessionTokenCmd cmd) {
        TokenDto token = refreshTokenCmdHandler.handle(cmd);
        return ResponseEntity.ok(new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "/api/v1/asiic/auth/refresh",
                ZonedDateTime.now(),
                "Token renovado exitosamente",
                token));
    }

    @PostMapping("/confirmar")
    @Operation(summary = "Confirmar cuenta creada")
    public ResponseEntity<ApiResponseDto<Void>> confirmarCuentaUsuario(@RequestBody ConfirmarCuentaCmd cmd) {
        confirmarCuentaCmdHandler.handle(cmd);
        return ResponseEntity.ok().body(new ApiResponseDto<>(
                HttpStatus.OK.value(), null,
                ZonedDateTime.now(),
                "Cuenta confirmada, ahora puedes iniciar sesión en tu cuenta", null));
    }
}
