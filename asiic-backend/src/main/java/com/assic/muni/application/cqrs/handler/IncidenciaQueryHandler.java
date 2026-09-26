package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.IncidenciaDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.mapper.IncidenciaMapper;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.domain.model.AsIncidencia;
import com.assic.muni.domain.repository.AsIncidenciaRepository;
import com.assic.muni.domain.repository.AsUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaQueryHandler {

    private final AsIncidenciaRepository incidenciaRepository;
    private final AsUsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    /**
     * Obtiene el detalle de una queja por su ID validando la pertenencia al vecino autenticado
     *
     * @param id ID de la incidencia
     * @return IncidenciaQuejaDto con sus relaciones *Obj mapeadas en objetos estructurados
     */
    @Transactional(readOnly = true)
    public IncidenciaDto obtenerMiIncidenciaPorId(Integer id) {
        final String subject = JwtExtractor.extrarJwtSubject();
        final int vecinoId = verificacionExistenciaVecino(subject);

        AsIncidencia incidencia = incidenciaRepository.findIncidenciaDetalleByIdAndVecino(id, vecinoId)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND,
                        "La queja no fue encontrada o no pertenece a la cuenta autenticada"));

        return IncidenciaMapper.entityToQuejaDto(incidencia, objectMapper);
    }

    public List<IncidenciaDto> obtenerMisIncidencias(){
        final String subject = JwtExtractor.extrarJwtSubject();
        final int vecinoId = verificacionExistenciaVecino(subject);
        List<AsIncidencia> incidencias = incidenciaRepository.findIncidenciasDetalleByVecino(vecinoId);
        if (incidencias.isEmpty()){
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron incidencias para el vecino");
        }
        return incidencias.stream().map(incidencia -> IncidenciaMapper.entityToQuejaDto(incidencia, objectMapper)).toList();
    }

    /**
     * Obtiene el detalle de una queja por su ID para perfiles administrativos / analistas
     *
     * @param id ID de la incidencia
     * @return IncidenciaQuejaDto con sus relaciones *Obj mapeadas en objetos estructurados
     */
    @Transactional(readOnly = true)
    public IncidenciaDto obtenerQuejaAdminPorId(Integer id) {
        AsIncidencia incidencia = incidenciaRepository.findQuejaDetalleById(id)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND,
                        "La queja no fue encontrada"));

        return IncidenciaMapper.entityToQuejaDto(incidencia, objectMapper);
    }

    /**
     * Verifica si el usuario existe en base de datos y obtiene el ID del vecino
     *
     * @param uuid UUID de la cuenta autenticada
     * @return ID del vecino
     */
    private Integer verificacionExistenciaVecino(String uuid) {
        return usuarioRepository.findUsPersonaByUsId(uuid)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST,
                        "No se pudo obtener información de la cuenta"));
    }
}
