package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.enums.ECatalogo;
import com.assic.muni.application.cqrs.dto.CatalogoDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.domain.model.AsCatalogo;
import com.assic.muni.domain.repository.AsCatalogoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogoQueryHandler {

    private final AsCatalogoRepository asCatalogoRepository;

    public static final Map<ECatalogo, String> mCatalogos = Map.of(
            ECatalogo.C_ESTADO_CIVIL, "as_estado_civil",
            ECatalogo.C_PROFESION, "as_profesion"
    );

    public static final Map<ECatalogo, String> mCatalogosPrivs = Map.of(
            ECatalogo.C_ESTADO_CIVIL, "as_estado_civil",
            ECatalogo.C_PROFESION, "as_profesion",
            ECatalogo.C_DEPENDENCIAS, "as_dependencias",
            ECatalogo.C_TIPOS_SERVICIO, "as_tipos_servicio",
            ECatalogo.C_TIPOS_DENUNCIA, "as_tipos_denuncia",
            ECatalogo.C_AREAS_SUGERENCIA, "as_areas"
    );


    public List<CatalogoDto> getCatalogoItemsByCatalogoId(ECatalogo catalogo) {
        String table = mCatalogos.get(catalogo);
        return getCatalogo(table);
    }

    public List<CatalogoDto> getCatalogoItemsPrivados(ECatalogo catalogo) {
        String table = mCatalogosPrivs.get(catalogo);
        return getCatalogo(table);
    }

    private List<CatalogoDto> getCatalogo(String tabla){
        List<AsCatalogo> list = asCatalogoRepository.findByCaTabla_TaNombreOrderByIdAsc(tabla);
        if (list.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No existen items para el catalogo");
        }
        return list.stream()
                .map(c -> new CatalogoDto(c.getId(), c.getCaValor()))
                .toList();
    }
}
