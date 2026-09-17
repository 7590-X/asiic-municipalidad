package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsVecino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VecinoRepository extends JpaRepository<AsVecino, Integer> {
}