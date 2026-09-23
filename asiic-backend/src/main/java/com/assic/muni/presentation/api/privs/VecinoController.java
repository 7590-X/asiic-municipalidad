package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.GetVecinoMeQuery;
import com.assic.muni.application.cqrs.dto.VecinoMeDto;
import com.assic.muni.application.cqrs.handler.GetVecinoMeQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Administración Privada de Vecino")
@RequestMapping("/api/v1/vecinos")
@RequiredArgsConstructor
public class VecinoController {

    private final GetVecinoMeQueryHandler getVecinoMeQueryHandler;

    @GetMapping("/me")
    @Operation(summary = "Obtener los datos del vecino autenticado")
    public ResponseEntity<VecinoMeDto> getVecinoMe(JwtAuthenticationToken principal) {
        String userId = principal.getToken().getSubject(); // Extrae el UUID real del claim 'sub' (Keycloak ID)
        VecinoMeDto dto = getVecinoMeQueryHandler.handle(new GetVecinoMeQuery(userId));
        return ResponseEntity.ok(dto);
    }
}
