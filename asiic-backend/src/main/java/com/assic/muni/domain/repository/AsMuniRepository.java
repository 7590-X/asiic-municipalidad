package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsMuni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AsMuniRepository extends JpaRepository<AsMuni, Short> {
    @Query("""
            select
            case
                when exists (select 1 from AsMuni m where m.asCorreo.coCorreo = :correo) then 1
                when exists (select 1 from AsMuni m where m.asDireccion.diLocacion = :municipio) then 2
                when exists (select 1 from AsMuni m where m.asPersona.peNit = :nit) then 3
                else 0
            end
            """)
    int validarMunicipalidad(String correo, Integer municipio, String nit);

}
