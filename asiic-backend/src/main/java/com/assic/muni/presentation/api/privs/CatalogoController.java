package com.assic.muni.presentation.api.privs;

import com.assic.muni.application.cqrs.dto.CatalogoDto;
import com.assic.muni.application.cqrs.handler.CatalogoQueryHandler;
import com.assic.muni.application.enums.ECatalogo;
import com.assic.muni.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/catalogos")
@Tag(name = "Catálogos Privados", description = "Listado de catálogos privados solo para usuario autenticados")
public class CatalogoController {

    private final CatalogoQueryHandler catalogoQueryHandler;

    @GetMapping("/{catalogo}")
    @SecurityRequirement(name = SwaggerConfig.SCHEME_NAME)
    @Operation(summary = "Obtener listado de items de un catalogo")
    public ResponseEntity<List<CatalogoDto>> obtener(@PathVariable ECatalogo catalogo) {
        return ResponseEntity.ok(catalogoQueryHandler.getCatalogoItemsPrivados(catalogo));
    }

}
