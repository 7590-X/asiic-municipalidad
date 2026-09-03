package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.cmd.RegistrarCatalogoCmd;
import com.assic.muni.application.cqrs.handler.RegistrarCatalogoCmdHandler;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.infrastructure.repository.AsCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final RegistrarCatalogoCmdHandler registrarCatalogoHandler;
    private final AsCatalogoRepository catalogoRepository;


    @GetMapping("/{catalogo}")
    public ResponseEntity<List<Object>> obtenerCatalogo(@PathVariable String catalogo) {
        List<Object> lst = switch (catalogo){
            case "ca1"  -> List.of();
            default -> throw new ServiceException(HttpStatus.BAD_REQUEST, "Catalogo invalido");
        };

        return ResponseEntity.ok(lst);
    }

    //Hacer el catálogo de las funciones clave valor, gestor cursivo para obtener la data, el parámetro de entrada y me genere la data
    //Lógica de programación ESP, mostrar Frontend, jdbc client

    //Validar que el OID Keyclook 201 de create
    //Almacenar la data
    //Generar un mensaje de 201 de Keyclook por medio de header location URL con la que puede acceder al recurso.
    //Path del recurso
    //Devolver la URI

    //Angular, simplicidad, framework clarityDesingSystem, Vware.
    //Estados.

 }