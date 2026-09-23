package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsVecinoDomicilio;
import com.assic.muni.domain.model.AsVecinoDomicilioId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsVecinoDomicilioRepository extends JpaRepository<AsVecinoDomicilio, AsVecinoDomicilioId> {
    Optional<AsVecinoDomicilio> findFirstByVdVecinoId(Integer vecinoId);
}
