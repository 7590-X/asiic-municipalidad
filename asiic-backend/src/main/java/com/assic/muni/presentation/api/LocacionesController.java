package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.dto.LocacionDto;
import com.assic.muni.application.cqrs.handler.LocacionesQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Catálogos de Locaciones")
@RequestMapping("/api/v1/asiic/public/locaciones")
public class LocacionesController {

    private final LocacionesQueryHandler locacionesQueryHandler;

    @GetMapping("/paises")
    @Operation(summary = "Obtener catálogo de países")
    public ResponseEntity<List<LocacionDto>> getAllPaises() {
        return ResponseEntity.ok(locacionesQueryHandler.obtainPaises());
    }

    @GetMapping("/departamentos")
    @Operation(summary = "Obtener catálogo de departamentos")
    public ResponseEntity<List<LocacionDto>> getAllDeptos(@RequestParam("pais_id") short paidId) {
        return ResponseEntity.ok(locacionesQueryHandler.obtainDeptos(paidId));
    }

    @GetMapping("/municipios")
    @Operation(summary = "Obtener catálogo de municipios")
    public ResponseEntity<List<LocacionDto>> getAllDeptos(@RequestParam("pais_id") short paidId,
                                                          @RequestParam("depto_id") short deptoId) {
        return ResponseEntity.ok(locacionesQueryHandler.obtainMunis(paidId, deptoId));
    }

    @GetMapping("/comunas")
    @Operation(summary = "Obtener catálogo de comunas")
    public ResponseEntity<List<LocacionDto>> getAllComunas(@RequestParam("pais_id") short paidId,
                                                           @RequestParam("depto_id") short deptoId,
                                                           @RequestParam("muni_id") short muniId) {
        return ResponseEntity.ok(locacionesQueryHandler.obtainComunas(paidId, deptoId, muniId));
    }
}
