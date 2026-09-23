package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsInsidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsInsidenciaRepository extends JpaRepository<AsInsidencia, Integer> {
    List<AsInsidencia> findByInUsrRegistroOrderByInFecRegistroDesc(String inUsrRegistro);
}
