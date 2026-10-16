package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsIncidenciaArchivo;
import com.assic.muni.domain.model.AsIncidenciaArchivoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsIncidenciaArchivoRepository extends JpaRepository<AsIncidenciaArchivo, AsIncidenciaArchivoId> {
     boolean existsById_AiIncidenciaAndId_AiArchivoAndAsIncidencia_InAnalista(int incidenciaId, int archivoId, String analistaId);
}
