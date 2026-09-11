package com.assic.muni;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import jakarta.annotation.PostConstruct;

@EnableAsync
@SpringBootApplication
@EnableJpaAuditing
public class AssicSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssicSystemApplication.class, args);
    }

    @PostConstruct
    public void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guatemala"));
    }
}
