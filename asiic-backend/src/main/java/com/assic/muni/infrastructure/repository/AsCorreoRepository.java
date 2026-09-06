package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsCorreo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsCorreoRepository extends JpaRepository<AsCorreo, Integer> {

    boolean existsByCoCorreo(String coCorreo);

}
