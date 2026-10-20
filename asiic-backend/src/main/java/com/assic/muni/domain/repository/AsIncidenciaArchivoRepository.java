package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsIncidenciaArchivo;
import com.assic.muni.domain.model.AsIncidenciaArchivoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsIncidenciaArchivoRepository extends JpaRepository<AsIncidenciaArchivo, AsIncidenciaArchivoId> {
    @Query("""
            select exists(
                select 1 from AsIncidenciaArchivo ia
                where ia.id.aiIncidencia = :incidenciaId
                  and ia.id.aiArchivo = :archivoId
                  and (ia.asIncidencia.inAnalistaId = :analistaId or ia.asIncidencia.inAgente = :analistaId)
            )
            """)
    boolean existsById_AiIncidenciaAndId_AiArchivoAndAsIncidencia_InAnalista(
            @Param("incidenciaId") int incidenciaId,
            @Param("archivoId") int archivoId,
            @Param("analistaId") String analistaId);

    boolean existsById_AiArchivoAndId_AiIncidencia(int archivoId, int incidenciaId);
}
