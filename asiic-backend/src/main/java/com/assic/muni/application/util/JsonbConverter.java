package com.assic.muni.application.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public final class JsonbConverter {
    private final ObjectMapper objectMapper;

    public <T> Map<String, Object> convertPojoToMap(T object) {
        return objectMapper.convertValue(
                object,
                new TypeReference<Map<String, Object>>() {
                }
        );
    }

    public <T> T convertMapToPojo(Map<String, Object> map) {
        return objectMapper.convertValue(
                map,
                (Class<T>) map.get("class")
        );
    }
}
