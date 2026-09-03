package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.RegistrarCatalogoCmd;
import com.assic.muni.domain.model.AsCatalogo;
import com.assic.muni.domain.model.AsTabla;
import com.assic.muni.infrastructure.repository.AsCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegistrarCatalogoCmdHandler implements CQRSHandler<String, RegistrarCatalogoCmd> {

    private final AsCatalogoRepository catalogoRepository;

    @Override
    @Transactional
    public String handle(RegistrarCatalogoCmd cmd) {

        // 1. Instanciar la tabla padre por su ID
        AsTabla tablaReferencia = new AsTabla();
        tablaReferencia.setId(BigDecimal.valueOf(cmd.getIdTabla())); // Asume que el ID de AsTabla es Short

        // 2. Mapear los datos a tu entidad real
        AsCatalogo nuevoCatalogo = new AsCatalogo();
        nuevoCatalogo.setId(cmd.getId());
        nuevoCatalogo.setCaTabla(tablaReferencia);
        nuevoCatalogo.setCaValor(cmd.getValor());
        nuevoCatalogo.setCaFecRegistro(Instant.now());

        // 3. Grabar en la base de datos
        catalogoRepository.save(nuevoCatalogo);

        return "Catálogo registrado exitosamente";
    }
}