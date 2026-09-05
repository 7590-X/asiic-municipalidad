package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.dto.CatalogoItemDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.infrastructure.repository.AsCatalogoRepository;
import com.assic.muni.infrastructure.repository.LocacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asiic/public/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final AsCatalogoRepository catalogoRepository;
    private final LocacionRepository locacionRepository;

    @GetMapping("/{nombre}")
    public ResponseEntity<List<CatalogoItemDto>> obtener(@PathVariable String nombre) {
        String tabla = switch (nombre) {
            case "estado-civil" -> "as_estado_civil";
            case "profesion"    -> "as_profesion";
            default -> throw new ServiceException(HttpStatus.BAD_REQUEST, "Catálogo inválido");
        };
        return ResponseEntity.ok(
                catalogoRepository.findByCaTabla_TaNombreOrderByIdAsc(tabla).stream()
                        .map(c -> new CatalogoItemDto(c.getId(), c.getCaValor()))
                        .toList());
    }

    @GetMapping("/zonas")
    public ResponseEntity<List<CatalogoItemDto>> zonas() {
        return ResponseEntity.ok(
                locacionRepository.findByLoComunaGreaterThanOrderByLoComunaAsc((short) 0).stream()
                        .map(l -> new CatalogoItemDto(l.getId(), l.getLoDescripcion()))
                        .toList());
    }
}