package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsDireccione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireccionRepository extends JpaRepository<AsDireccione, Integer> {
}