package com.assic.muni.application.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public final class JsonbConverter {
    private final ObjectMapper objectMapper;

    // Para POJOs / Objetos individuales: { ... }
    public <T> Map<String, Object> convertPojoToMap(T object) {
        if (object == null) return null;
        return objectMapper.convertValue(
                object,
                new TypeReference<Map<String, Object>>() {
                }
        );
    }

    // Para Listas / Colecciones: [ ... ]
    public <T> List<Map<String, Object>> convertListToMapList(List<T> list) {
        if (list == null) return null;
        return objectMapper.convertValue(
                list,
                new TypeReference<List<Map<String, Object>>>() {
                }
        );
    }

    // Para convertir a una estructura JSON genérica (admite Map o List)
    public Object convertPojoToJsonObject(Object object) {
        if (object == null) return null;
        return objectMapper.convertValue(
                object,
                Object.class
        );
    }
}