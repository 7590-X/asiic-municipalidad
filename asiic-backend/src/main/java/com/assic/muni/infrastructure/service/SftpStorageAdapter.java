package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.SftpStoragePort;
import com.assic.muni.infrastructure.config.SftpProperties;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de infraestructura que implementa SftpStoragePort usando Spring Integration SFTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SftpStorageAdapter implements SftpStoragePort {

    private final SftpRemoteFileTemplate sftpRemoteFileTemplate;
    private final SftpProperties sftpProperties;

    @Override
    public String uploadFile(InputStream inputStream, String remoteFilename, String subDirectory) {
        if (inputStream == null) {
            throw new InfrastructureException(HttpStatus.BAD_REQUEST, "El stream del archivo no puede ser nulo");
        }

        String sanitizedFilename = sanitizeFilename(remoteFilename);
        String targetDirectory = resolveTargetDirectory(subDirectory);
        String fullPath = targetDirectory + "/" + sanitizedFilename;

        try {
            sftpRemoteFileTemplate.execute(session -> {
                makeDirectories(session, targetDirectory);
                session.write(inputStream, fullPath);
                return null;
            });
            log.info("Archivo almacenado exitosamente en SFTP: {}", fullPath);
            return fullPath;
        } catch (Exception e) {
            log.error("Error al subir archivo '{}' al SFTP: {}", fullPath, e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al almacenar archivo en servidor SFTP");
        }
    }

    @Override
    public String uploadFile(byte[] data, String remoteFilename, String subDirectory) {
        if (data == null || data.length == 0) {
            throw new InfrastructureException(HttpStatus.BAD_REQUEST, "El contenido del archivo no puede estar vacío");
        }
        return uploadFile(new ByteArrayInputStream(data), remoteFilename, subDirectory);
    }

    @Override
    public String uploadFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new InfrastructureException(HttpStatus.BAD_REQUEST, "El archivo proporcionado está vacío o es nulo");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            return uploadFile(inputStream, uniqueFilename, subDirectory);
        } catch (IOException e) {
            log.error("Error al leer el archivo multipart: {}", e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el archivo recibido");
        }
    }

    @Override
    public byte[] downloadFile(String remoteFilePath) {
        if (!StringUtils.hasText(remoteFilePath)) {
            throw new InfrastructureException(HttpStatus.BAD_REQUEST, "La ruta del archivo remoto es requerida");
        }

        String fullPath = normalizePath(remoteFilePath);

        try {
            return sftpRemoteFileTemplate.execute(session -> {
                if (!session.exists(fullPath)) {
                    throw new InfrastructureException(HttpStatus.NOT_FOUND,
                            "El archivo solicitado no fue encontrado en el servidor SFTP");
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                session.read(fullPath, outputStream);
                return outputStream.toByteArray();
            });
        } catch (InfrastructureException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al descargar archivo '{}' desde SFTP: {}", fullPath, e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al recuperar archivo desde servidor SFTP");
        }
    }

    @Override
    public InputStream downloadFileInputStream(String remoteFilePath) {
        byte[] content = downloadFile(remoteFilePath);
        return new ByteArrayInputStream(content);
    }

    @Override
    public boolean deleteFile(String remoteFilePath) {
        if (!StringUtils.hasText(remoteFilePath)) {
            return false;
        }

        String fullPath = normalizePath(remoteFilePath);

        try {
            return Boolean.TRUE.equals(sftpRemoteFileTemplate.execute(session -> {
                if (session.exists(fullPath)) {
                    boolean removed = session.remove(fullPath);
                    if (removed) {
                        log.info("Archivo eliminado exitosamente de SFTP: {}", fullPath);
                    }
                    return removed;
                }
                log.warn("No se encontró el archivo '{}' en SFTP para eliminar", fullPath);
                return false;
            }));
        } catch (Exception e) {
            log.error("Error al eliminar archivo '{}' en SFTP: {}", fullPath, e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al eliminar archivo del servidor SFTP");
        }
    }

    @Override
    public boolean exists(String remoteFilePath) {
        if (!StringUtils.hasText(remoteFilePath)) {
            return false;
        }

        String fullPath = normalizePath(remoteFilePath);

        try {
            return Boolean.TRUE.equals(sftpRemoteFileTemplate.execute(session -> session.exists(fullPath)));
        } catch (Exception e) {
            log.error("Error al verificar existencia de '{}' en SFTP: {}", fullPath, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listFiles(String subDirectory) {
        String targetDirectory = resolveTargetDirectory(subDirectory);

        try {
            return sftpRemoteFileTemplate.execute(session -> {
                if (!session.exists(targetDirectory)) {
                    return Collections.emptyList();
                }
                String[] fileNames = session.listNames(targetDirectory);
                if (fileNames == null) {
                    return Collections.emptyList();
                }
                return Arrays.stream(fileNames)
                        .filter(name -> !".".equals(name) && !"..".equals(name))
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            log.error("Error al listar archivos en '{}' desde SFTP: {}", targetDirectory, e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al listar archivos del servidor SFTP");
        }
    }

    /**
     * Crea los directorios remotos de manera jerárquica si no existen.
     */
    private void makeDirectories(Session<?> session, String path) throws IOException {
        if (!StringUtils.hasText(path)) {
            return;
        }

        String normalized = path.replace('\\', '/');
        String[] segments = normalized.split("/");
        StringBuilder currentPath = new StringBuilder(normalized.startsWith("/") ? "/" : "");

        for (String segment : segments) {
            if (!segment.isEmpty()) {
                if (currentPath.length() > 1 && !currentPath.toString().endsWith("/")) {
                    currentPath.append("/");
                }
                currentPath.append(segment);
                String dir = currentPath.toString();
                if (!session.exists(dir)) {
                    try {
                        session.mkdir(dir);
                        log.debug("Directorio creado en SFTP: {}", dir);
                    } catch (Exception e) {
                        // Puede haber sido creado por concurrencia
                        log.trace("Directorio {} ya existe o no pudo crearse: {}", dir, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Sanitiza el nombre del archivo para prevenir ataques de Path Traversal.
     */
    private String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return UUID.randomUUID().toString();
        }
        String cleanName = Paths.get(filename).getFileName().toString();
        return cleanName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Resuelve el directorio destino combinando la raíz configurada con el subdirectorio.
     */
    private String resolveTargetDirectory(String subDirectory) {
        String base = sftpProperties.getRemoteDirectory();
        if (!StringUtils.hasText(base)) {
            base = "/";
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!StringUtils.hasText(subDirectory)) {
            return base;
        }

        String sub = subDirectory.trim().replace('\\', '/');
        if (!sub.startsWith("/")) {
            sub = "/" + sub;
        }
        if (sub.endsWith("/")) {
            sub = sub.substring(0, sub.length() - 1);
        }
        return base + sub;
    }

    /**
     * Normaliza la ruta remota para asegurar que sea absoluta respecto al servidor SFTP.
     */
    private String normalizePath(String remoteFilePath) {
        String cleanPath = remoteFilePath.trim().replace('\\', '/');
        String base = sftpProperties.getRemoteDirectory();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        if (cleanPath.startsWith(base)) {
            return cleanPath;
        }
        if (!cleanPath.startsWith("/")) {
            cleanPath = "/" + cleanPath;
        }
        return base + cleanPath;
    }
}
