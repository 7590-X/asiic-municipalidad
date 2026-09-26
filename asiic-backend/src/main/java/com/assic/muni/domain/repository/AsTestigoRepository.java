package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsTestigo;
import com.assic.muni.domain.model.AsTestigoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsTestigoRepository extends JpaRepository<AsTestigo, AsTestigoId> {
}
