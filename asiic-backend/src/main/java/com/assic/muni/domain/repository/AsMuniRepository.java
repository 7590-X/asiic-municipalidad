package com.assic.muni.domain.repository;

import com.assic.muni.application.cqrs.dto.MuniDto;
import com.assic.muni.domain.model.AsMuni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    @Query("""
            select new com.assic.muni.application.cqrs.dto.MuniDto(
                m.id,
                m.muFecFundacion,
                m.muLatitud,
                m.muLongitud,
                m.muPbx,
                m.asCorreo.coCorreo,
                m.asDireccion.diDireccion,
                m.asPersona.peNombre,
                m.asPersona.peNit,
                m.asDireccion.diLocacionObj.loDescripcion
            )
            from AsMuni m
            where m.muEstado = 'A'
            """)
    List<MuniDto> findAllMunis();

    @Query("""
            select new com.assic.muni.application.cqrs.dto.MuniDto(
                m.id,
                m.muFecFundacion,
                m.muLatitud,
                m.muLongitud,
                m.muPbx,
                m.asCorreo.coCorreo,
                m.asDireccion.diDireccion,
                m.asPersona.peNombre,
                m.asPersona.peNit,
                m.asDireccion.diLocacionObj.loDescripcion
            )
            from AsMuni m
            where
                m.id = :muniId
                and m.muEstado = 'A'
            """)
    Optional<MuniDto> findMuniDtoById(@Param("muniId") Short muniId);
}
