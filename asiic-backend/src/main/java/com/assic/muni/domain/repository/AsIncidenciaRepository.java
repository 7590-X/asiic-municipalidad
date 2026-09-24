package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsIncidenciaRepository extends JpaRepository<AsIncidencia, Integer> {

    Optional<AsIncidencia> findByIdAndInVecino(int incidenciaId, int vecinoId);
}
