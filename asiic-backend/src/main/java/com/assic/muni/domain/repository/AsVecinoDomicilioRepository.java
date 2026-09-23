package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsVecinoDomicilio;
import com.assic.muni.domain.model.AsVecinoDomicilioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsVecinoDomicilioRepository extends JpaRepository<AsVecinoDomicilio, AsVecinoDomicilioId> {

    @Query("""
            select 1 from AsVecinoDomicilio t where t.id.vdContador = :contador and t.id.vdVecino = :vecinoId
            """)
    boolean existsRelationByContadorAndVecino(@Param("contador") String contador, @Param("vecinoId") int vecinoId);
}
