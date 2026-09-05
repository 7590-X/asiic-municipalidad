package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.dto.CatalogoItemDto;
import com.assic.muni.application.cqrs.enums.ECatalogo;
import com.assic.muni.application.cqrs.query.CatalogoQuery;
import com.assic.muni.infrastructure.repository.LocacionRepository;
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
public class CatalogoController {

    private final CatalogoQuery catalogoQuery;
    private final LocacionRepository locacionRepository;

    @GetMapping("/{catalogo}")
    @Operation(summary = "Obtener listado de items de un catalogo")
    public ResponseEntity<List<CatalogoItemDto>> obtener(@PathVariable ECatalogo catalogo) {
        return ResponseEntity.ok(catalogoQuery.getCatalogoItemsByCatalogoId(catalogo));
    }

    @GetMapping("/zonas")
    public ResponseEntity<List<CatalogoItemDto>> zonas() {
        return ResponseEntity.ok(
                locacionRepository.findByLoComunaGreaterThanOrderByLoComunaAsc((short) 0).stream()
                        .map(l -> new CatalogoItemDto(l.getId(), l.getLoDescripcion()))
                        .toList());
    }
}