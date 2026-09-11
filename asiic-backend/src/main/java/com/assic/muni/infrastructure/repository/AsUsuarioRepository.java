package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsUsuarioRepository extends JpaRepository<AsUsuario,String> {
}
