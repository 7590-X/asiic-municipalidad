package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.IncidenciaPayloadCmd;
import com.assic.muni.application.cqrs.dto.ResumenDto;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.dto.ArchivoDto;
import com.assic.muni.application.cqrs.dto.IncidenciaDto;
import com.assic.muni.application.cqrs.dto.IncidenciaFeedback;
import com.assic.muni.application.cqrs.handler.ArchivosQueryHandler;
import com.assic.muni.application.cqrs.handler.IncidenciaPipelineHandler;
import com.assic.muni.application.cqrs.handler.ResumenQueryHandler;
import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
@Tag(name = "Incidencias Pipeline", description = "Administra todos los eventos que se pueden efectuar para cambios de estado de incidencias")
@RequestMapping("/api/v1/asiic/incidencias")
public class IncidenciasEventController {

    private final IncidenciaPipelineHandler incidenciaPipelineHandler;
    private final ArchivosQueryHandler archivosQueryHandler;
    private final ResumenQueryHandler resumenQueryHandler;

    @PostMapping("/{id}/event/enviar-incidencia")
    @Operation(summary = "EV1-0 - Enviar Incidencia")
    public ResponseEntity<ApiResponseDto> eventEnviarIncidencia(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.ENVIAR_INCIDENCIA);
        return ResponseEntity.ok(getMessageBody("Incidencia enviada exitosamente"));
    }

    @PostMapping("/{id}/event/aceptar-incidencia")
    @Operation(summary = "EV1-1-1 - Aceptar Incidencia")
    public ResponseEntity<ApiResponseDto> eventAceptarIncidencia(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.ACEPTAR_INCIDENCIA);
        return ResponseEntity.ok(getMessageBody("Incidencia aceptada correctamente"));
    }

    @PostMapping("/{id}/event/rechazar-incidencia")
    @Operation(summary = "EV1-1-2 - Rechazar Incidencia")
    public ResponseEntity<ApiResponseDto> eventRechazadaIncidencia(@PathVariable Integer id,
            @Valid @RequestBody List<IncidenciaFeedback> feedback) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.RECHAZAR_INCIDENCIA, feedback);
        return ResponseEntity.ok(getMessageBody("Incidencia aceptada correctamente"));
    }

    @PostMapping("/{id}/event/apelar-incidencia")
    @Operation(summary = "EV1-1-2-1 - Apelar Incidencia")
    public ResponseEntity<ApiResponseDto> eventApelarIncidencia(
            @PathVariable Integer id,
            @Parameter(description = "Datos estructurados de la incidencia en formato JSON", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = IncidenciaPayloadCmd.class))) @RequestPart("insidencia_json") IncidenciaPayloadCmd payload,

            @Parameter(description = "Lista opcional de archivos adjuntos (imágenes, documentos, etc.)", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) @RequestPart(value = "insidencia_bin", required = false) List<MultipartFile> evidencias) {
        incidenciaPipelineHandler.pipelineApelarIncidencia(id, payload, evidencias);
        return ResponseEntity.ok(getMessageBody("Incidencia apelada correctamente"));
    }

    @PostMapping("/{id}/event/finalizar-recoleccion")
    @Operation(summary = "EV1-2 - Finalizar Recolección")
    public ResponseEntity<ApiResponseDto> eventTerminarRecoleccion(@PathVariable Integer id,
            @Valid @RequestBody ResumenDto resumen) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.FINALIZAR_RECOLECCION, resumen);
        return ResponseEntity.ok(getMessageBody("Recolección finalizada correctamente"));
    }

    @GetMapping("/{id}/resumenes")
    public ResponseEntity<List<ResumenDto>> obtenerResumenDeCampo(@PathVariable Integer id) {
        return ResponseEntity.ok(resumenQueryHandler.obtenerResumenIncidencia(id));
    }

    @PostMapping("/{id}/event/confirmar-incidencia")
    @Operation(summary = "EV1-3 - Confirmar Incidencia")
    public ResponseEntity<ApiResponseDto> eventConfirmarIncidencia(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.CONFIRMAR_INCIDENCIA);
        return ResponseEntity.ok(getMessageBody("Incidencia confirmada correctamente"));
    }

    @PostMapping("/{id}/event/aperturar-incidencia")
    @Operation(summary = "EV1-4 - Aperturar Incidencia")
    public ResponseEntity<ApiResponseDto> eventAperturarIncidencia(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.APERTURAR_CASO);
        return ResponseEntity.ok(getMessageBody("Incidencia aperturada correctamente"));
    }

    @PostMapping("/{id}/event/iniciar-solucion")
    @Operation(summary = "EV1-5 - Iniciar Solución")
    public ResponseEntity<ApiResponseDto> eventIniciarSolucion(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.INICIAR_CASO);
        return ResponseEntity.ok(getMessageBody("Incidencia iniciada para solución"));
    }

    @PostMapping("/{id}/event/finalizar-solucion")
    @Operation(summary = "EV1-6 - Finalizar Solución")
    public ResponseEntity<ApiResponseDto> eventFinalizarSolucion(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.CONFIRMAR_SOLUCION);
        return ResponseEntity.ok(getMessageBody("Recolección finalizada correctamente"));
    }

    @PostMapping("/{id}/event/finalizar-incidencia")
    @Operation(summary = "EV1-7 - Finalizar Solución")
    public ResponseEntity<ApiResponseDto> eventFinalizarIncidencia(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.FINALIZAR);
        return ResponseEntity.ok(getMessageBody("Recolección finalizada correctamente"));
    }

    @GetMapping("/asignadas/analista")
    @Operation(summary = "Obtener incidencias asignadas al analista autenticado")
    public ResponseEntity<List<IncidenciaDto>> getIncidenciasAsignadas() {
        return ResponseEntity.ok(
                incidenciaPipelineHandler.obtenerIncidenciasAsignadasByEstado(
                        IncidenciaState.ASIGNADA,
                        IncidenciaState.ACEPTADA,
                        IncidenciaState.ASIGNADA_CAMPO,
                        IncidenciaState.RECOLECCION_FIN,
                        IncidenciaState.CONFIRMADA));
    }

    @GetMapping("/{id}/archivos")
    @Operation(summary = "Obtener archivos de incidencia asignada al analista autenticado")
    public ResponseEntity<List<ArchivoDto>> obtenerArchivosDeIncidenciaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(archivosQueryHandler.obtenerArchivosDeIncidenciaAsignadaAnalista(id));
    }

    @GetMapping("/{id}/archivos/{archivoId}")
    @Operation(summary = "Descargar archivo de una incidencia por su ID como analista", description = "Permite descargar el archivo binario asociado a una incidencia si la incidencia esta asignada al analista autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo descargado exitosamente", headers = {
                    @Header(name = HttpHeaders.CONTENT_DISPOSITION, description = "Metadata de descarga con el nombre del archivo (attachment; filename=\"...\")", schema = @Schema(type = "string"))
            }, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "204", description = "El archivo solicitado existe pero no tiene contenido (vacío)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Incidencia o archivo no encontrado o no pertenece al usuario", content = @Content)
    })
    public ResponseEntity<Resource> obtenerArchivosDeIncidenciaPorId(@PathVariable Integer id,
            @PathVariable Integer archivoId) {
        ArchivoDto dto = archivosQueryHandler.descargarArchivoComoAnalista(id, archivoId);
        return getResourceResponseEntity(dto);
    }

    @NonNull
    static ResponseEntity<Resource> getResourceResponseEntity(ArchivoDto dto) {
        if (dto.contenido().length == 0) {
            return ResponseEntity.noContent().build();
        }

        ByteArrayResource resource = new ByteArrayResource(dto.contenido());
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(dto.nombre(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(dto.contenido().length)
                .body(resource);
    }

    private ApiResponseDto getMessageBody(String message) {
        return new ApiResponseDto(200, HttpStatus.OK.getReasonPhrase(), ZonedDateTime.now(), message, null);
    }
}
