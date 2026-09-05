package com.assic.muni.infrastructure.service;

import com.assic.muni.application.cqrs.dto.CatalogoItemDto;
import com.assic.muni.application.cqrs.enums.ECatalogo;
import com.assic.muni.application.cqrs.query.CatalogoQuery;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.model.AsCatalogo;
import com.assic.muni.infrastructure.repository.AsCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogoQueryService implements CatalogoQuery {

    private final AsCatalogoRepository asCatalogoRepository;

    public static final Map<ECatalogo, String> mCatalogos = Map.of(
            ECatalogo.C_ESTADO_CIVIL, "as_estado_civil",
            ECatalogo.C_PROFESION, "as_profesion"
    );

    @Override
    public List<CatalogoItemDto> getCatalogoItemsByCatalogoId(ECatalogo catalogo) {
        String table = mCatalogos.get(catalogo);
        List<AsCatalogo> list = asCatalogoRepository.findByCaTabla_TaNombreOrderByIdAsc(table);
        if (list.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No existen items para el catalogo " + catalogo.getValue());
        }
        return list.stream()
                .map(c -> new CatalogoItemDto(c.getId(), c.getCaValor()))
                .toList();
    }
}
