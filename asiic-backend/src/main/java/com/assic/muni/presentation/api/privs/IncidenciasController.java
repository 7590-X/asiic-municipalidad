package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.handler.UpsertIncidenciaCmdHandler;
import com.assic.muni.presentation.api.util.UriBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.net.URI;
import java.time.ZonedDateTime;

@RestController
@RequestMapping(IncidenciasController.URI)
@RequiredArgsConstructor
@Tag(name = "Administración de insidencias")
public class IncidenciasController {

    protected static final String URI = "/api/v1/asiic/incidencias";
    private final UpsertIncidenciaCmdHandler upsertIncidenciaCmdHandler;

    @PostMapping
    @Operation(summary = "Crear una nueva incidencia con evidencias")
    public ResponseEntity<ApiResponseDto> crearIncidencia(
            @RequestPart("insidencia_json") IncidenciaPayloadCmd payload,
            @RequestPart(value = "insidencia_bin", required = false) List<MultipartFile> evidencias) {
        int resourceId = upsertIncidenciaCmdHandler.handle(payload, evidencias);
        URI resourceUri = UriBuilder.build(resourceId);
        return ResponseEntity.created(resourceUri)
                .body(new ApiResponseDto(HttpStatus.CREATED.value(), null, ZonedDateTime.now(),
                        "Insidencia registrada exitosamente", null));
    }
}
