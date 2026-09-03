package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsLocacione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocacionRepository extends JpaRepository<AsLocacione, Integer> {

    Optional<AsLocacione> findFirstByLoDeptoAndLoMuniAndLoComuna(Short loDepto, Short loMuni, Short loComuna);
}