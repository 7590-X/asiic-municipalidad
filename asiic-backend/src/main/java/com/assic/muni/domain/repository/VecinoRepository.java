package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsVecino;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VecinoRepository extends JpaRepository<AsVecino, Integer> {
}