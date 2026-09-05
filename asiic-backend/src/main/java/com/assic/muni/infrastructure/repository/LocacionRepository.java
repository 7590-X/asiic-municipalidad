package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsLocacione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocacionRepository extends JpaRepository<AsLocacione, Integer> {
    List<AsLocacione> findByLoComunaGreaterThanOrderByLoComunaAsc(Short loComuna);
}