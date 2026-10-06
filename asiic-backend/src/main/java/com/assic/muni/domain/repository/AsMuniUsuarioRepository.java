package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsMuniUsuario;
import com.assic.muni.domain.model.AsMuniUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsMuniUsuarioRepository extends JpaRepository<AsMuniUsuario, AsMuniUsuarioId> {
}
