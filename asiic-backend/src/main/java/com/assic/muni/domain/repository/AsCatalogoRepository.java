package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AsCatalogoRepository extends JpaRepository<AsCatalogo, Short> {

    java.util.List<AsCatalogo> findByCaTabla_TaNombreOrderByIdAsc(String taNombre);

    java.util.Optional<AsCatalogo> findFirstByCaTabla_TaNombreAndCaValorIgnoreCase(String taNombre, String caValor);

    Optional<AsCatalogo> findByCaSeudo(String caSeudo);
}