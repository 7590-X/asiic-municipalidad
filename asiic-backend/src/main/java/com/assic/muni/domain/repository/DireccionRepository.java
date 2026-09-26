package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsDireccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionRepository extends JpaRepository<AsDireccion, Integer> {
}