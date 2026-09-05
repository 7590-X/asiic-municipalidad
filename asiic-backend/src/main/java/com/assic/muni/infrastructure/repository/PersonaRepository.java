package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsPersona;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersonaRepository extends JpaRepository<AsPersona, Integer> {

    boolean existsByPeCui(String peCui);

    @Query(value = """
        select case
        when exists(select 1 from as_personas where pe_cui = :p_cui) then 1
        when exists(select 1 from as_correos where co_correo = :p_correo) then 2
        else 0
        end as codigo_estado
    """, nativeQuery = true)
    int validateByPeCuiAndPeCoCorreo(@Param("p_cui") String peCui, @Param("p_correo") String coCorreo);
}
