package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.CrearIncidenciaCmd;
import com.assic.muni.application.cqrs.cmd.GetMisIncidenciasQuery;
import com.assic.muni.application.cqrs.dto.IncidenciaPayloadDto;
import com.assic.muni.application.cqrs.dto.IncidenciaResumenDto;
import com.assic.muni.application.cqrs.handler.CrearIncidenciaCmdHandler;
import com.assic.muni.application.cqrs.handler.GetMisIncidenciasQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incidencias")
@RequiredArgsConstructor
@Tag(name = "Incidencias Privadas")
public class IncidenciasController {

    private final CrearIncidenciaCmdHandler crearIncidenciaCmdHandler;
    private final GetMisIncidenciasQueryHandler getMisIncidenciasQueryHandler;
    private final com.assic.muni.application.cqrs.handler.GetIncidenciaByIdQueryHandler getIncidenciaByIdQueryHandler;
    private final com.assic.muni.application.cqrs.handler.ActualizarIncidenciaCmdHandler actualizarIncidenciaCmdHandler;

    @PostMapping
    @Operation(summary = "Crear una nueva incidencia con evidencias")
    public ResponseEntity<Map<String, String>> crearIncidencia(
            @RequestPart("datos") IncidenciaPayloadDto payload,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias,
            JwtAuthenticationToken principal) throws Exception {

        String userId = principal.getToken().getSubject();
        
        CrearIncidenciaCmd cmd = new CrearIncidenciaCmd(userId, payload, evidencias);
        String trackingCode = crearIncidenciaCmdHandler.handle(cmd);
        
        return ResponseEntity.ok(Collections.singletonMap("codigo", trackingCode));
    }

    @GetMapping
    @Operation(summary = "Obtener listado de incidencias del vecino")
    public ResponseEntity<List<IncidenciaResumenDto>> obtenerMisIncidencias(JwtAuthenticationToken principal) {
        String userId = principal.getToken().getSubject();
        List<IncidenciaResumenDto> list = getMisIncidenciasQueryHandler.handle(new GetMisIncidenciasQuery(userId));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de una incidencia")
    public ResponseEntity<com.assic.muni.application.cqrs.dto.IncidenciaDetalleDto> obtenerIncidenciaPorId(
            @PathVariable String id,
            JwtAuthenticationToken principal) {
        String userId = principal.getToken().getSubject();
        com.assic.muni.application.cqrs.dto.IncidenciaDetalleDto dto = getIncidenciaByIdQueryHandler.handle(
                new com.assic.muni.application.cqrs.query.GetIncidenciaByIdQuery(id, userId));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un borrador de incidencia")
    public ResponseEntity<Map<String, String>> actualizarIncidencia(
            @PathVariable String id,
            @RequestPart("datos") IncidenciaPayloadDto payload,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias,
            JwtAuthenticationToken principal) {
        String userId = principal.getToken().getSubject();
        com.assic.muni.application.cqrs.cmd.ActualizarIncidenciaCmd cmd = 
                new com.assic.muni.application.cqrs.cmd.ActualizarIncidenciaCmd(id, userId, payload, evidencias);
        String trackingCode = actualizarIncidenciaCmdHandler.handle(cmd);
        return ResponseEntity.ok(Collections.singletonMap("codigo", trackingCode));
    }
}
