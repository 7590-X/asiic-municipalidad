package com.assic.muni.infrastructure.repository;

import com.assic.muni.domain.model.AsCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsCatalogoRepository extends JpaRepository<AsCatalogo, Short> {

    java.util.List<AsCatalogo> findByCaTabla_TaNombreOrderByIdAsc(String taNombre);
    java.util.Optional<AsCatalogo> findFirstByCaTabla_TaNombreAndCaValorIgnoreCase(String taNombre, String caValor);
    Optional<AsCatalogo> findByCaSeudo(String caSeudo);
}