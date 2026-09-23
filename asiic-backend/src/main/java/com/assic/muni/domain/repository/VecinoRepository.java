package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsVecino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VecinoRepository extends JpaRepository<AsVecino, Integer> {
    Optional<AsVecino> findByVeUsrRegistro(String userId);
}