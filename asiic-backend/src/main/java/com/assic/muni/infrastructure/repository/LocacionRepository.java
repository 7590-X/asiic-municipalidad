package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsLocacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocacionRepository extends JpaRepository<AsLocacion, Integer> {
    List<AsLocacion> findByLoComunaGreaterThanOrderByLoComunaAsc(Short loComuna);

    @Query("""
        select p from AsLocacion p
        where p.loPais != 0 and p.loDepto = 0 and p.loMuni = 0 and p.loComuna = 0
        """)
    List<AsLocacion> findAllPaises();

    @Query("""
        select p from AsLocacion p
        where p.loPais = :loPais and p.loDepto != 0 and p.loMuni = 0 and p.loComuna = 0
        """)
    List<AsLocacion> findAllDeptos(int loPais);

    @Query("""
        select p from AsLocacion p
        where p.loPais = :loPais and p.loDepto = :loDepto and p.loMuni = 0 and p.loComuna = 0
        """)
    List<AsLocacion> findAllMunis(int loPais, int loDepto);

    @Query("""
        select p from AsLocacion p
        where p.loPais = :loPais and p.loDepto = :loDepto and p.loMuni = :loMuni and p.loComuna != 0
        """)
    List<AsLocacion> findAllComunas(int loPais, int loDepto, int  loMuni);
}