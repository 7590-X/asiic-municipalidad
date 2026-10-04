package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsCorreo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AsCorreoRepository extends JpaRepository<AsCorreo, Integer> {

    boolean existsByCoCorreo(String coCorreo);

    Optional<AsCorreo> findByCoCorreo(String coCorreo);

    @Query("""
            select c from AsCorreo c join AsMuni m on m.id = :muniId and m.muCorreo = c.id
            """)
    Optional<AsCorreo> findByJoinAsMuni(Short muniId);
}
