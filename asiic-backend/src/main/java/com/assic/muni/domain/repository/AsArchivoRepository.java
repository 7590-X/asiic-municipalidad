package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsArchivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsArchivoRepository extends JpaRepository<AsArchivo, Integer> {

    Optional<AsArchivo> findByArHash(String arHash);
}
