package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsVecino;
import com.assic.muni.domain.model.AsVecinoDomicilio;
import com.assic.muni.domain.model.AsVecinoDomicilioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsVecinoDomicilioRepository extends JpaRepository<AsVecinoDomicilio, AsVecinoDomicilioId> {

    @Query("""
            select count(t) > 0 from AsVecinoDomicilio t where t.id.vdContador = :contador and t.id.vdVecino = :vecinoId
            """)
    boolean existsRelationByContadorAndVecino(@Param("contador") String contador,
                                              @Param("vecinoId") Integer vecinoId);
}
