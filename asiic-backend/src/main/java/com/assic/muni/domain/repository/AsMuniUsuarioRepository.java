package com.assic.muni.domain.repository;

import com.assic.muni.application.cqrs.dto.MuniDto;
import com.assic.muni.domain.model.AsMuniUsuario;
import com.assic.muni.domain.model.AsMuniUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsMuniUsuarioRepository extends JpaRepository<AsMuniUsuario, AsMuniUsuarioId> {

    @Query("""
            select new com.assic.muni.application.cqrs.dto.MuniDto(
                m.id,
                m.muFecFundacion,
                m.muLatitud,
                m.muLongitud,
                m.muPbx,
                m.asCorreo.coCorreo,
                m.asDireccion.diDireccion,
                m.asPersona.peNombre,
                m.asPersona.peNit,
                m.asDireccion.diLocacionObj.loDescripcion
            )
            from AsMuniUsuario mu join mu.muMuni m where mu.id.muUsuario = :usuarioId
            """)
    List<MuniDto> findByAllMuniDtoByUsuarioId(@Param("usuarioId") String usuarioId);
}
