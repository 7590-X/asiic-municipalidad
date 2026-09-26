package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.dto.DomicilioDto;
import com.assic.muni.application.cqrs.handler.DomicilioQueryHandler;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Domicilios", description = "Gestión de domicilios")
@RequestMapping("/api/v1/asiic/domicilios")
public class DomiciliosController {

    private final DomicilioQueryHandler domicilioQueryHandler;

    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @GetMapping("/me")
    @Operation(summary = "Obtener los domicilios del vecino autenticado")
    public ResponseEntity<List<DomicilioDto>> obtenerMisDomicilios() {
        return ResponseEntity.ok(domicilioQueryHandler.obtenerMisDomicilios());
    }

    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @GetMapping("/me/{contador}")
    @Operation(summary = "Obtener el domicilio del vecino autenticado con el contador dado")
    public ResponseEntity<DomicilioDto> obtenerMiDomicilio(@PathVariable String contador) {
        return ResponseEntity.ok(domicilioQueryHandler.obtenerMiDomicilio(contador));
    }
}
