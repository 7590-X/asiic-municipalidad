package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsDomicilio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AsDomicilioRepository extends JpaRepository<AsDomicilio, String> {

    @Query("""
                select d from AsDomicilio d
                inner join AsVecinoDomicilio vd on d.doContador = vd.id.vdContador and vd.id.vdVecino = :vecinoId
                order by d.doFecRegistro
            """)
    List<AsDomicilio> findByVecinoId(Integer vecinoId);

    @Query("""
                select d from AsDomicilio d
                inner join AsVecinoDomicilio vd on
                        d.doContador    = vd.id.vdContador
                    and vd.id.vdVecino  = :vecinoId
                where d.doContador = :contador
                order by d.doFecRegistro
            """)
    List<AsDomicilio> findByVecinoIdAndContador(Integer vecinoId, String contador);
}
