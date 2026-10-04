package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsDireccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AsDireccionRepository extends JpaRepository<AsDireccion, Integer> {

    @Query("""
                select d from AsDireccion d inner join AsMuni m on m.id = :muniId and m.muDireccion = d.id
            """)
    Optional<AsDireccion> findByJoinMuni(short muniId);

}