package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.MuniPayloadCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.dto.MuniDto;
import com.assic.muni.application.cqrs.handler.MunisQueryHandler;
import com.assic.muni.application.cqrs.handler.UpsertMunicipalidadCmdHandler;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import com.assic.muni.presentation.api.util.UriBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/municipalidades")
@Tag(name = "Municipalidad", description = "Gestionar municipalidades")
public class MuniController {

    private final UpsertMunicipalidadCmdHandler upsertMunicipalidadCmdHandler;
    private final MunisQueryHandler munisQueryHandler;

    @PostMapping
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Upsert para municipalidad")
    public ResponseEntity<ApiResponseDto> upsertMunicipalidad(@Valid @RequestBody MuniPayloadCmd cmd) {
        int muniId = upsertMunicipalidadCmdHandler.handle(cmd);
        URI uri = UriBuilder.build(muniId);
        return ResponseEntity.created(uri).body(
                new ApiResponseDto(
                        HttpStatus.CREATED.value(),
                        HttpStatus.CREATED.getReasonPhrase(),
                        ZonedDateTime.now(),
                        "Municipalidad Registrada Correctamente",
                        null)
        );
    }


    @GetMapping
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener municipalidades registradas")
    public ResponseEntity<List<MuniDto>> getMunis() {
        return ResponseEntity.ok(munisQueryHandler.obtenerMunis());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener municipalidad por id")
    public ResponseEntity<MuniDto> getMuni(@PathVariable("id") Short muniId) {
        return ResponseEntity.ok(munisQueryHandler.obtenerMuniById(muniId));
    }
}
