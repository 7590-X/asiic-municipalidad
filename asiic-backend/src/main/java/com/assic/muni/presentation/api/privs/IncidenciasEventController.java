package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.handler.IncidenciaPipelineHandler;
import com.assic.muni.domain.enums.IncidenciaEvent;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
@Tag(name = "Incidencias Pipeline", description = "Administra todos los eventos que se pueden efectuar para cambios de estado de incidencias")
@RequestMapping("/api/v1/asiic/incidencias")
public class IncidenciasEventController {

    private final IncidenciaPipelineHandler incidenciaPipelineHandler;

    @PostMapping("/{id}/event/enviar-incidencia")
    @Operation(summary = "EV1-0 - Enviar Incidencia")
    public ResponseEntity<ApiResponseDto> eventEnviarIncidencia(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.ENVIAR_INCIDENCIA);
        return ResponseEntity.ok(getMessageBody("Incidencia enviada exitosamente"));
    }

    @PostMapping("/{id}/event/aceptar-incidencia")
    @Operation(summary = "EV1-1 - Aceptar Incidencia")
    public ResponseEntity<ApiResponseDto> eventAceptarIncidencia(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.ACEPTAR_INCIDENCIA);
        return ResponseEntity.ok(getMessageBody("Incidencia aceptada correctamente"));
    }


    @PostMapping("/{id}/event/finalizar-recoleccion")
    @Operation(summary = "EV1-2 - Finalizar Recolección")
    public ResponseEntity<ApiResponseDto> eventTerminarRecoleccion(@PathVariable Integer id) {
        incidenciaPipelineHandler.pipelineChangeState(id, IncidenciaEvent.FINALIZAR_RECOLECCION);
        return ResponseEntity.ok(getMessageBody("Recolección finalizada correctamente"));
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

    private ApiResponseDto getMessageBody(String message) {
        return new ApiResponseDto(200, HttpStatus.OK.getReasonPhrase(), ZonedDateTime.now(), message, null);
    }
}
