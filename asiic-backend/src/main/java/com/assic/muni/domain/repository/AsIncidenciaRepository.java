package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsIncidenciaRepository extends JpaRepository<AsIncidencia, Integer> {

    Optional<AsIncidencia> findByIdAndInVecino(int incidenciaId, int vecinoId);

    @Query("""
        SELECT i FROM AsIncidencia i
        LEFT JOIN FETCH i.inTipoIncidenciaObj
        LEFT JOIN FETCH i.inPrivacidadObj
        LEFT JOIN FETCH i.inUnidadObj
        LEFT JOIN FETCH i.inVecinoObj v
        LEFT JOIN FETCH v.vePersona
        LEFT JOIN FETCH v.veCorreo
        LEFT JOIN FETCH v.veTelefono
        LEFT JOIN FETCH i.inContadorObj c
        LEFT JOIN FETCH c.doDireccion
        WHERE i.id = :id AND i.inVecino = :vecinoId
    """)
    Optional<AsIncidencia> findIncidenciaDetalleByIdAndVecino(@Param("id") int id, @Param("vecinoId") int vecinoId);

    @Query("""
        SELECT i FROM AsIncidencia i
        LEFT JOIN FETCH i.inTipoIncidenciaObj
        LEFT JOIN FETCH i.inPrivacidadObj
        LEFT JOIN FETCH i.inUnidadObj
        LEFT JOIN FETCH i.inVecinoObj v
        LEFT JOIN FETCH v.vePersona
        LEFT JOIN FETCH v.veCorreo
        LEFT JOIN FETCH v.veTelefono
        LEFT JOIN FETCH i.inContadorObj c
        LEFT JOIN FETCH c.doDireccion
        WHERE i.inVecino = :vecinoId
    """)
    List<AsIncidencia> findIncidenciasDetalleByVecino(@Param("vecinoId") int vecinoId);

    @Query("""
        SELECT i FROM AsIncidencia i
        LEFT JOIN FETCH i.inTipoIncidenciaObj
        LEFT JOIN FETCH i.inPrivacidadObj
        LEFT JOIN FETCH i.inUnidadObj
        LEFT JOIN FETCH i.inVecinoObj v
        LEFT JOIN FETCH v.vePersona
        LEFT JOIN FETCH v.veCorreo
        LEFT JOIN FETCH v.veTelefono
        LEFT JOIN FETCH i.inContadorObj c
        LEFT JOIN FETCH c.doDireccion
        WHERE i.id = :id AND i.inTipoIncidencia = 1
    """)
    Optional<AsIncidencia> findQuejaDetalleById(@Param("id") int id);
}
