package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.dto.ArchivoDto;
import com.assic.muni.application.cqrs.dto.IncidenciaDto;
import com.assic.muni.application.cqrs.dto.IncidenciaFeedback;
import com.assic.muni.application.cqrs.handler.ArchivosQueryHandler;
import com.assic.muni.application.cqrs.handler.IncidenciaQueryHandler;
import com.assic.muni.application.cqrs.handler.UpsertIncidenciaCmdHandler;
import com.assic.muni.application.cqrs.handler.UpsertIncidenciaEvidenciaCmdHandler;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import com.assic.muni.presentation.api.util.UriBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.net.URI;
import java.time.ZonedDateTime;

import static com.assic.muni.presentation.api.privs.IncidenciasEventController.getResourceResponseEntity;

@RestController
@RequestMapping(IncidenciasController.URI)
@RequiredArgsConstructor
@Tag(name = "Incidencias", description = "Gestión de incidencias")
public class IncidenciasController {

    protected static final String URI = "/api/v1/asiic/incidencias";
    private final UpsertIncidenciaCmdHandler upsertIncidenciaCmdHandler;
    private final UpsertIncidenciaEvidenciaCmdHandler upsertIncidenciaEvidenciaCmdHandler;
    private final IncidenciaQueryHandler incidenciaQueryHandler;
    private final ArchivosQueryHandler archivosQueryHandler;

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
        int resourceId = upsertIncidenciaCmdHandler.handle(payload);

        URI resourceUri = UriBuilder.build(resourceId);
        return ResponseEntity.created(resourceUri)
                .body(new ApiResponseDto(HttpStatus.CREATED.value(), null, ZonedDateTime.now(),
                        "Incidence registrada exitosamente", null));
    }

    @PostMapping(path = "/me/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Cargar evidencias para una incidencia")
    public ResponseEntity<ApiResponseDto> cargarEvidencias(
            @PathVariable("id") Integer id,
            @Parameter(
                    description = "Lista de archivos adjuntos (imágenes, documentos, etc.)",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestBody List<MultipartFile> evidencias) {
        upsertIncidenciaEvidenciaCmdHandler.handle(id, evidencias);
        return ResponseEntity.ok(new ApiResponseDto(HttpStatus.OK.value(), null, ZonedDateTime.now(),
                "Evidencias cargadas exitosamente", null));
    }

    @GetMapping("/me/{id}")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener una de mis incidencias emitidas por su ID")
    public ResponseEntity<IncidenciaDto> obtenerMiIncidenciaPorId(@PathVariable Integer id) {
        IncidenciaDto dto = incidenciaQueryHandler.obtenerMiIncidenciaPorId(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener mis incidencias emitidas")
    public ResponseEntity<List<IncidenciaDto>> obtenerMisIncidenciasPorId() {
        List<IncidenciaDto> dtos = incidenciaQueryHandler.obtenerMisIncidencias();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/me/{incidenciaId}/archivos")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener mis archivos de una incidencia por su ID")
    public ResponseEntity<List<ArchivoDto>> obtenerArchivosDeIncidenciaPorId(@PathVariable Integer incidenciaId) {
        return ResponseEntity.ok(archivosQueryHandler.obtenerMisArchivosDeIncidencia(incidenciaId));
    }

    @Operation(
            summary = "Descargar mi archivo de una incidencia por su ID",
            description = "Permite descargar el archivo binario asociado a una incidencia si pertenece al usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo descargado exitosamente",
                    headers = {
                            @Header(
                                    name = HttpHeaders.CONTENT_DISPOSITION,
                                    description = "Metadata de descarga con el nombre del archivo (attachment; filename=\"...\")",
                                    schema = @Schema(type = "string")
                            )
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "El archivo solicitado existe pero no tiene contenido (vacío)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Incidencia o archivo no encontrado o no pertenece al usuario",
                    content = @Content
            )
    })
    @GetMapping("/me/{incidenciaId}/archivos/{archivoId}")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    public ResponseEntity<Resource> descargarMiArchivoDeIncidencia(@PathVariable Integer incidenciaId, @PathVariable Integer archivoId) {
        ArchivoDto dto = archivosQueryHandler.descargarMiArchivo(incidenciaId, archivoId);

        return getResourceResponseEntity(dto);
    }

    @Operation(summary = "Elimina una evidencia de una incidencia")
    @DeleteMapping("me/{incidenciaId}/archivos/{archivoId}")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    public ResponseEntity<ApiResponseDto> eliminarEvidencia(@PathVariable Integer incidenciaId, @PathVariable Integer archivoId) {
        archivosQueryHandler.eliminarArchivoDeIncidencia(incidenciaId, archivoId);
        return ResponseEntity.ok()
                .body(new ApiResponseDto(HttpStatus.NO_CONTENT.value(), HttpStatus.NO_CONTENT.getReasonPhrase(), ZonedDateTime.now(),
                        "Evidencia eliminada correctamente", null));
    }

    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtiene el feedback de rechazo de una incidencia")
    @GetMapping("/me/{incidenciaId}/feedback/rechazo")
    public ResponseEntity<List<IncidenciaFeedback>> obtenerFeedbackRechazoIncidencia(@PathVariable Integer incidenciaId) {
        return ResponseEntity.ok(incidenciaQueryHandler.obtenerMiIncidenciaFeedback(incidenciaId));
    }
}
