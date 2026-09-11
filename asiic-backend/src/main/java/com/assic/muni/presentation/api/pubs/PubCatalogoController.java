package com.assic.muni.presentation.api.pubs;

import com.assic.muni.application.cqrs.dto.CatalogoDto;
import com.assic.muni.application.enums.ECatalogo;
import com.assic.muni.application.cqrs.handler.CatalogoQueryHandler;
import com.assic.muni.infrastructure.repository.AsLocacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asiic/public/catalogos")
@RequiredArgsConstructor
@Tag(name = "Catálogos públicos")
public class PubCatalogoController {

    private final CatalogoQueryHandler catalogoQueryHandler;

    @GetMapping("/{catalogo}")
    @Operation(summary = "Obtener listado de items de un catalogo")
    public ResponseEntity<List<CatalogoDto>> obtener(@PathVariable ECatalogo catalogo) {
        return ResponseEntity.ok(catalogoQueryHandler.getCatalogoItemsByCatalogoId(catalogo));
    }
}