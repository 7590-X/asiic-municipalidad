package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.dto.IncidenciaDto;
import com.assic.muni.application.cqrs.handler.UpsertIncidenciaCmdHandler;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import com.assic.muni.presentation.api.util.UriBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.net.URI;
import java.time.ZonedDateTime;

@RestController
@RequestMapping(IncidenciasController.URI)
@RequiredArgsConstructor
@Tag(name = "Reporte de Incidencias")
public class IncidenciasController {

    protected static final String URI = "/api/v1/asiic/incidencias";
    private final UpsertIncidenciaCmdHandler upsertIncidenciaCmdHandler;
    private final com.assic.muni.application.cqrs.handler.IncidenciaQueryHandler incidenciaQueryHandler;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Crear una nueva incidencia con evidencias")
    public ResponseEntity<ApiResponseDto> crearIncidencia(
            @Parameter(
                    description = "Datos estructurados de la incidencia en formato JSON",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = IncidenciaPayloadCmd.class))
            )
            @RequestPart("insidencia_json")
            IncidenciaPayloadCmd payload,

            @Parameter(
                    description = "Lista opcional de archivos adjuntos (imágenes, documentos, etc.)",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestPart(value = "insidencia_bin", required = false) List<MultipartFile> evidencias) {
        int resourceId = upsertIncidenciaCmdHandler.handle(payload, evidencias);
        URI resourceUri = UriBuilder.build(resourceId);
        return ResponseEntity.created(resourceUri)
                .body(new ApiResponseDto(HttpStatus.CREATED.value(), null, ZonedDateTime.now(),
                        "Insidencia registrada exitosamente", null));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener el detalle estructurado de una queja por su ID")
    public ResponseEntity<IncidenciaDto> obtenerMiInscidenciaPorId(@PathVariable Integer id) {
        IncidenciaDto dto = incidenciaQueryHandler.obtenerMiIncidenciaPorId(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener el detalle estructurado de una queja por su ID")
    public ResponseEntity<List<IncidenciaDto>> obtenerMisIncidenciasPorId() {
        List<IncidenciaDto> dtos = incidenciaQueryHandler.obtenerMisIncidencias();
        return ResponseEntity.ok(dtos);
    }
}
