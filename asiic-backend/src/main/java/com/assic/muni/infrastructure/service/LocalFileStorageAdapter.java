package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.FileStoragePort;
import com.assic.muni.domain.model.AsArchivo;
import com.assic.muni.domain.repository.AsArchivoRepository;
import com.assic.muni.infrastructure.exception.InfrastructureException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageAdapter implements FileStoragePort {

    private final AsArchivoRepository asArchivoRepository;

    @Value("${app.storage.local-path:/var/asiic/archivos}")
    private String localPath;

    @Override
    public AsArchivo storeFile(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(localPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            String hash = UUID.randomUUID().toString();
            String newFilename = hash + "." + extension;
            Path filePath = uploadDir.resolve(newFilename);

            // TODO: Integrar aquí el servicio SFTP

            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            AsArchivo archivo = new AsArchivo();
            archivo.setArNombre(originalFilename);
            archivo.setArPath(filePath.toString());
            archivo.setArFormato(extension.length() > 5 ? extension.substring(0, 5) : extension);
            archivo.setArHash(hash);
            archivo.setArVersion(java.math.BigDecimal.valueOf(1));
            archivo.setArFecRegistro(Instant.now());

            return asArchivoRepository.save(archivo);

        } catch (IOException e) {
            log.error("Error al guardar archivo localmente", e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el archivo adjunto");
        }
    }
}
