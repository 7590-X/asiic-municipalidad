package com.assic.muni.presentation.api.util;

import java.net.URI;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public final class UriBuilder {

    /**
     * Construye la URI del nuevo recurso agregando "/{id}" a la ruta de la petición
     * actual.
     * 
     * Ejemplo: Si la petición fue POST a
     * "http://localhost:8080/api/v1/asiic/incidencias"
     * y el id es 25, la URI resultante será:
     * "http://localhost:8080/api/v1/asiic/incidencias/25"
     *
     * @param id Identificador único del recurso creado
     * @return URI completa para el header Location
     */
    public static URI build(Object id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }

    /**
     * Permite especificar una plantilla de ruta personalizada y sus variables.
     *
     * @param pathTemplate Plantilla relativa, por ejemplo: "/{id}/detalle" o
     *                     "/{id}"
     * @param uriVariables Variables para expandir en los placeholders
     * @return URI completa para el header Location
     */
    public static URI build(String pathTemplate, Object... uriVariables) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path(pathTemplate)
                .buildAndExpand(uriVariables)
                .toUri();
    }

}
