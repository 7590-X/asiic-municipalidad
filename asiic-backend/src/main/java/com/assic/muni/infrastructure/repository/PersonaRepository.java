package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsPersona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<AsPersona, Integer> {

    boolean existsByPeCui(String peCui);
}
