package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.enums.ENumero;
import com.assic.muni.application.cqrs.handler.NumerosQueryHandler;
import com.assic.muni.application.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asiic/public/validacion-numeros")
@Tag(name = "Validaciones de valores de identidad personal")
public class ValidacionNumeroController {

    private final NumerosQueryHandler numerosQueryHandler;

    @GetMapping
    @Operation(summary = "Validaciones de diferentes tipo de valores numéricos")
    public ResponseEntity<Void> validacionNumeros(@RequestParam("key") ENumero key,
                                                  @RequestParam("value") String value) {
        switch (key) {
            case N_CUI -> numerosQueryHandler.validarDisponibilidadCUI(value);
            case N_NIT -> numerosQueryHandler.validarDisponibilidadNIT(value);
            case N_CORREO -> numerosQueryHandler.validarDisponibilidadCorreo(value);
            default -> throw new ServiceException(HttpStatus.BAD_REQUEST,"Validación no disponible");
        }
        return ResponseEntity.ok().build();
    }
}
