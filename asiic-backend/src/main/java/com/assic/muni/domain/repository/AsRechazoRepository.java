package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsRechazo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AsRechazoRepository extends JpaRepository<AsRechazo, Integer> {

    @Query("select r from AsRechazo r join r.asIncidencia i where r.reIncidencia = :incidencia and i.inVecino = :vecino order by r.reNumero desc limit 1")
    Optional<AsRechazo> findByReIncidenciaOnlyMax(@Param("incidencia") Integer reIncidencia, @Param("vecino") int vecinoId);
}
