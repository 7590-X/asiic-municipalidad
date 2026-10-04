package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.cmd.MuniPayloadCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.ZonedDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/municipalidades")
@Tag(name = "Municipalidad", description = "Gestionar municipalidades")
public class MuniController {

    private final UpsertMunicipalidadCmdHandler upsertMunicipalidadCmdHandler;

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
}
