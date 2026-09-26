package com.assic.muni.domain.repository;

import com.assic.muni.domain.model.AsCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AsCatalogoRepository extends JpaRepository<AsCatalogo, Short> {

    java.util.List<AsCatalogo> findByCaTabla_TaNombreOrderByIdAsc(String taNombre);

    Optional<AsCatalogo> findByCaSeudo(String caSeudo);

    @Query("""
            select d.id from AsCatalogo d where d.caSeudo = :caSeudo
                    """)
    Optional<Short> findIdByCaSeudo(@Param("caSeudo") String caSeudo);

    @Query("""
            select count(c) > 0 from AsCatalogo c where c.caTabla.id = :tabla and c.id = :id
                """)
    boolean existsByTableAndId(@Param("tabla") short tabla, @Param("id") short id);

}