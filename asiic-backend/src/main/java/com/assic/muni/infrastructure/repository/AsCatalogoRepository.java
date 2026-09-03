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

    @Query(value = "SELECT clave, valor FROM fn_obtener_catalogo_kv(:nombreTabla)", nativeQuery = true)
    List<Object[]> obtenerClaveValor(@Param("nombreTabla") String nombreTabla);

    Optional<AsCatalogo> findFirstByCaTabla_TaNombreAndCaValorIgnoreCase(String taNombre, String caValor);
}