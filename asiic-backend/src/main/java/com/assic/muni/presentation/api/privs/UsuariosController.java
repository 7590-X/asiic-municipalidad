package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.RegistrarUsuarioCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.handler.RegistrarUsuarioCmdHandler;
import com.assic.muni.application.cqrs.handler.UsuarioQueryHandler;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import com.assic.muni.presentation.api.util.UriBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.ZonedDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/usuarios")
@SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
public class UsuariosController {

    private final RegistrarUsuarioCmdHandler registrarUsuarioCmdHandler;
    private final UsuarioQueryHandler usuarioQueryHandler;

    @GetMapping
    @Operation(summary = "Obtiene el listado de usuarios")
    public ResponseEntity<Object> getUsuarios() {
        return ResponseEntity.ok(usuarioQueryHandler.getUsuarios());
    }

    @GetMapping("/roles")
    @Operation(summary = "Obtiene los roles permitidos para el usuario")
    public ResponseEntity<Object> getRolesPermitidos() {
        return ResponseEntity.ok(usuarioQueryHandler.getRolesPermitidos());
    }

    @PostMapping
    @Operation(summary = "Crea una cuenta de usuario")
    public ResponseEntity<ApiResponseDto> postCrearCuentaUsuario(@RequestBody RegistrarUsuarioCmd cmd) {
        final String id = registrarUsuarioCmdHandler.handle(cmd);
        URI uri = UriBuilder.build(id);
        return ResponseEntity.created(uri).body(new ApiResponseDto(
                HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(),
                ZonedDateTime.now(), "Cuenta creada exitosamente", null
        ));
    }
}