package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AsArchivoRepository extends JpaRepository<AsArchivo, Integer> {

    Optional<AsArchivo> findByArHash(String arHash);

    @Query("""
            select a from AsArchivo a
            inner join AsIncidenciaArchivo ia
                on a.id                 = ia.id.aiArchivo
                and ia.id.aiIncidencia  = :incidenciaId
                and ia.aiUsrRegistro    = :user
            """)
    List<AsArchivo> findByIncidenciaIdAndUserUUID(Integer incidenciaId, String user);

    @Query("""
            select a from AsArchivo a
            inner join AsIncidenciaArchivo ia
                on a.id                 = ia.id.aiArchivo
                and ia.id.aiIncidencia  = :incidenciaId
            """)
    List<AsArchivo> findByIncidenciaId(Integer incidenciaId);


    @Query("""
            select a from AsArchivo a
            inner join AsIncidenciaArchivo ia
                on a.id                 = ia.id.aiArchivo
                and ia.id.aiIncidencia  = :incidenciaId
                and ia.aiUsrRegistro    = :user
            where a.id = :archivoId
            """)
    Optional<AsArchivo> findByIncidenciaAndArchivoAndUserUUID(Integer incidenciaId, Integer archivoId, String user);
}
