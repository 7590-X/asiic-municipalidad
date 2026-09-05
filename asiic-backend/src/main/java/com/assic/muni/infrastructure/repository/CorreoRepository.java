package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsCorreo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorreoRepository extends JpaRepository<AsCorreo, Integer> {
}
