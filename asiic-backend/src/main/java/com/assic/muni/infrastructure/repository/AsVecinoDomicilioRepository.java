package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsVecinoDomicilio;
import com.assic.muni.domain.model.AsVecinoDomicilioId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsVecinoDomicilioRepository extends JpaRepository<AsVecinoDomicilio, AsVecinoDomicilioId> {
}
