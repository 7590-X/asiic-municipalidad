package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsTelefono;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelefonoRepository extends JpaRepository<AsTelefono, Integer> {

}
