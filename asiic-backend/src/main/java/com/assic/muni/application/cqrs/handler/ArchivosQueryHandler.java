package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.dto.ArchivoDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.mapper.ArchivoMapper;
import com.assic.muni.application.port.out.FileStoragePort;
import com.assic.muni.application.port.out.SftpStoragePort;
import com.assic.muni.application.util.JwtExtractor;
import com.assic.muni.domain.model.AsArchivo;
import com.assic.muni.domain.repository.AsArchivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArchivosQueryHandler {

    private final AsArchivoRepository archivoRepository;
    private final FileStoragePort fileStoragePort;


    public List<ArchivoDto> obtenerMisArchivosDeIncidencia(int incidenciaId) {
        String subject = JwtExtractor.extrarJwtSubject();
        List<AsArchivo> archivos = archivoRepository.findByIncidenciaIdAndUserUUID(incidenciaId, subject);
        if (archivos.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron archivos para la incidencia");
        }
        return archivos.stream().map(ArchivoMapper::frontEntityToDto).toList();
    }

    public List<ArchivoDto> obtenerArchivosDeIncidencia(int incidenciaId) {
        List<AsArchivo> archivos = archivoRepository.findByIncidenciaId(incidenciaId);
        if (archivos.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontraron archivos para la incidencia");
        }
        return archivos.stream().map(ArchivoMapper::frontEntityToDto).toList();
    }

    public ArchivoDto descargarMiArchivo(int incidenciaId, int archivoId) {
        String subject = JwtExtractor.extrarJwtSubject();
        Optional<AsArchivo> archivo = archivoRepository.findByIncidenciaAndArchivoAndUserUUID(incidenciaId, archivoId, subject);
        if (archivo.isEmpty()) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No se encontró el archivo");
        }
        byte[] b = fileStoragePort.getFile(archivo.get().getId());
        return ArchivoMapper.frontEntityToDto(archivo.get(), b);
    }
}
