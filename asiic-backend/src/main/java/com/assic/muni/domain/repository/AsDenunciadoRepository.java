package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsDenunciado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.assic.muni.domain.model.AsIncidencia;

public interface AsDenunciadoRepository extends JpaRepository<AsDenunciado, Integer> {
}
