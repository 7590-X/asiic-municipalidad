package com.assic.muni.infrastructure.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidenciasCronJobs {

    private final JdbcTemplate jdbcTemplate;

    // Cada 1 minuto
    @Scheduled(fixedDelay = 60000)
    void asignarAnalista() {
        try {
            log.info("Asignando analistas a incidencias");
            jdbcTemplate.execute("call sp_asignacion_analista()");
            log.info("Analistas asignados");
        } catch (Exception e) {
            log.error("Error al asignar analistas", e);
        }
    }

    // Cada 1 minuto
    @Scheduled(fixedDelay = 60000)
    void asignarAgenteCampo() {
        try {
            log.info("Asignando agentes de campo a incidencias");
            jdbcTemplate.execute("call sp_asignacion_agente()");
            log.info("Agentes de campo asignados");
        } catch (Exception e) {
            log.error("Error al asignar agentes de campo", e);
        }
    }
}
