package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsUsuario;

import feign.Param;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AsUsuarioRepository extends JpaRepository<AsUsuario, String> {

    @Query("""
            select u.usPersona.id from AsUsuario u where u.usId = :uuid
            """)
    Optional<Integer> findUsPersonaByUsId(@Param("uuid") String uuid);

}
