package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsUsuario;

import feign.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AsUsuarioRepository extends JpaRepository<AsUsuario, String> {

    @Query("""
            select u.usPersona.id from AsUsuario u where u.usId = :uuid
            """)
    Optional<Integer> findUsPersonaByUsId(@Param("uuid") String uuid);

    /**
     * Validar existencia de usuario por correo
     *
     * @param correo Correo del usuario
     * @return True = Existe, False = No existe
     */
    boolean existsByUsCorreo_CoCorreo(String correo);

    List<AsUsuario> findAllByUsEstadoAndUsTipo_Id(String usEstado, Short caId);
}
