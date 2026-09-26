package com.assic.muni.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.assic.muni.domain.model.AsVecino;

public interface AsVecinoRepository extends JpaRepository<AsVecino, Integer> {

    @Query("""
            select v.veCorreo.coCorreo from AsVecino v where v.id = :veId
            """)
    Optional<String> findEmailByVeId(@Param("veId") int veId);
}
