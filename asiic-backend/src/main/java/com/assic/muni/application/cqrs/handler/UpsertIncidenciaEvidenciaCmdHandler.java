package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.FileStoragePort;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.domain.enums.IncidenciaState;
import com.assic.muni.domain.model.AsArchivo;
import com.assic.muni.domain.model.AsIncidenciaArchivo;
import com.assic.muni.domain.model.AsIncidenciaArchivoId;
import com.assic.muni.domain.repository.AsIncidenciaArchivoRepository;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpsertIncidenciaEvidenciaCmdHandler {

    private final AsIncidenciaArchivoRepository incidenciaArchivoRepository;
    private final AsIncidenciaRepository incidenciaRepository;
    private final FileStoragePort fileStoragePort;
    private final static List<IncidenciaState> ESTADOS_VALIDOS = List.of(IncidenciaState.BORRADOR, IncidenciaState.RECHAZADA);

    public void handle(int incidenciaId, List<MultipartFile> evidencias) {
        final String userId = JwtExtractor.extrarJwtSubject();
        // Validar si el usuario es dueño de la incidencia
        final boolean esDueno = incidenciaRepository.existsByIdAndInUsrRegistro(incidenciaId, userId);
        if (!esDueno) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "La incidencia no pertenece al usuario");
        }
        // Validar estado de incidencias para cargar archivos
        final boolean esValido = incidenciaRepository.existsByIdAndInEstadoIn(incidenciaId, ESTADOS_VALIDOS);
        if(!esValido){
            throw new ServiceException(HttpStatus.BAD_REQUEST, "La incidencia no se encuentra en un estado válido para la carga de evidencias");
        }

        if (evidencias != null && !evidencias.isEmpty()) {
            for (MultipartFile mf : evidencias) {
                AsArchivo archivo = fileStoragePort.storeFile(mf);

                incidenciaArchivoRepository.save(
                        AsIncidenciaArchivo.builder()
                                .id(new AsIncidenciaArchivoId(archivo.getId(), incidenciaId))
                                .aiEstado("A")
                                .build()
                );
            }
        }
    }
}
