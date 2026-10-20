package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsResumen;
import com.assic.muni.domain.model.AsResumenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AsResumenRepository extends JpaRepository<AsResumen, AsResumenId> {

    @Query("select coalesce(d.id.reVersion,0) from AsResumen d where d.id.reIncidencia = :id")
    Optional<Integer> findCountByIdReIncidencia(@Param("id") Integer idReIncidencia);
    
}