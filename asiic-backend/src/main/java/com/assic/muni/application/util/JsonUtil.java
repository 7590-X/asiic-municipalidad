package com.assic.muni.application.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonUtil {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
