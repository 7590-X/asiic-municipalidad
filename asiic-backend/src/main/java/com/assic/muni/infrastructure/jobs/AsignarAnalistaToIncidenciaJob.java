package com.assic.muni.infrastructure.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsignarAnalistaToIncidenciaJob {

    private final JdbcTemplate jdbcTemplate;

    // Cada 5 minutos
    @Scheduled(fixedDelay = 300000)
    void asignar() {
        try {
            log.info("Asignando analista a incidencias");
            jdbcTemplate.execute("call sp_asignacion_analista()");
            log.info("Analistas asignados");
        } catch (Exception e) {
            log.error("Error al asignar analistas", e);
        }
    }
}
