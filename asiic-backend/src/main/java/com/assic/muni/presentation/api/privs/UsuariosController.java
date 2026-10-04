package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.handler.UsuarioQueryHandler;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/usuarios")
@SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
public class UsuariosController {

    private final UsuarioQueryHandler usuarioQueryHandler;

    @GetMapping("/roles")
    @Operation(summary = "Obtiene los roles permitidos para el usuario")
    public ResponseEntity<Object> getRolesPermitidos() {
        return ResponseEntity.ok(usuarioQueryHandler.getRolesPermitidos());
    }
}