package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.FileStoragePort;
import com.assic.muni.application.port.out.SftpStoragePort;
import com.assic.muni.domain.model.AsArchivo;
import com.assic.muni.domain.repository.AsArchivoRepository;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class SftpFileStorageAdapter implements FileStoragePort {

    private static final BigDecimal DEFAULT_VERSION = BigDecimal.ONE;
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private static final int BUFFER_SIZE = 32 * 1024; // 32 KB

    private final SftpStoragePort sftpStoragePort;
    private final AsArchivoRepository asArchivoRepository;

    @Override
    public AsArchivo storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("[STORE-FILE] Intento de subida rechazado: el archivo recibido es nulo o está vacío");
            throw new InfrastructureException(HttpStatus.BAD_REQUEST, "El archivo a almacenar está vacío o es nulo");
        }

        long startTime = System.currentTimeMillis();
        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();

        log.info("[STORE-FILE] Iniciando procesamiento de archivo: '{}' ({} bytes)", originalFilename, fileSize);

        try {
            // Cálculo de hash para verificación de integridad y deduplicación
            String calculatedHash = calculateSha256(file);
            log.debug("[STORE-FILE] SHA-256 generado para '{}': {}", originalFilename, calculatedHash);

            // Comprobar deduplicación
            Optional<AsArchivo> existingFile = asArchivoRepository.findByArHash(calculatedHash);
            if (existingFile.isPresent()) {
                AsArchivo persisted = existingFile.get();
                log.info("[STORE-FILE] Deduplicación exitosa: El archivo '{}' ya existe con ID {} (Hash: {}). Reutilizando registro en {} ms",
                        originalFilename, persisted.getId(), calculatedHash, (System.currentTimeMillis() - startTime));
                return persisted;
            }

            // Preparación de nombres y rutas remotas
            String extension = extractExtension(originalFilename);
            String fileUid = UUID.randomUUID().toString();
            String remoteFilename = extension.isEmpty() ? fileUid : fileUid + "." + extension;

            log.debug("[STORE-FILE] Transfiriendo archivo al servidor SFTP como '{}'...", remoteFilename);

            String remotePath;
            try (InputStream is = new BufferedInputStream(file.getInputStream(), BUFFER_SIZE)) {
                remotePath = sftpStoragePort.uploadFile(is, remoteFilename, "");
            }

            log.info("[STORE-FILE] Archivo transferido a SFTP con éxito. Ruta remota: '{}'", remotePath);

            // Persistencia en Base de Datos
            AsArchivo archivo = new AsArchivo();
            archivo.setArPath(remotePath);
            archivo.setArNombre(originalFilename != null ? originalFilename : remoteFilename);
            archivo.setArFormato(extension.length() > 5 ? extension.substring(0, 5) : extension);
            archivo.setArHash(calculatedHash);
            archivo.setArVersion(DEFAULT_VERSION);

            AsArchivo saved = asArchivoRepository.save(archivo);

            log.info("[STORE-FILE] Archivo registrado exitosamente con ID {} en BD. Tiempo total: {} ms",
                    saved.getId(), (System.currentTimeMillis() - startTime));

            return saved;

        } catch (NoSuchAlgorithmException e) {
            log.error("[STORE-FILE] Error crítico: Algoritmo de hash no soportado en la JVM", e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Configuración de seguridad inválida");
        } catch (IOException e) {
            log.error("[STORE-FILE] Error de I/O al procesar/transferir '{}': {}", originalFilename, e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar archivo para SFTP");
        } catch (Exception e) {
            log.error("[STORE-FILE] Error no controlado al almacenar '{}': {}", originalFilename, e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado al almacenar el archivo");
        }
    }

    @Override
    public byte[] getFile(Integer archivoId) {
        log.info("[GET-FILE] Solicitando archivo con ID: {}", archivoId);

        AsArchivo archivo = asArchivoRepository.findById(archivoId)
                .orElseThrow(() -> {
                    log.warn("[GET-FILE] Archivo no encontrado en base de datos para ID: {}", archivoId);
                    return new InfrastructureException(HttpStatus.NOT_FOUND, "Registro de archivo no encontrado");
                });

        log.debug("[GET-FILE] Descargando desde SFTP en la ruta: '{}'", archivo.getArPath());
        long startTime = System.currentTimeMillis();

        byte[] content = sftpStoragePort.downloadFile(archivo.getArPath());

        log.info("[GET-FILE] Archivo ID {} descargado exitosamente ({} bytes) en {} ms",
                archivoId, (content != null ? content.length : 0), (System.currentTimeMillis() - startTime));

        return content;
    }

    @Override
    public boolean deleteFile(Integer archivoId) {
        log.info("[DELETE-FILE] Iniciando proceso de eliminación para archivo ID: {}", archivoId);

        return asArchivoRepository.findById(archivoId).map(archivo -> {
            boolean deletedInSftp = sftpStoragePort.deleteFile(archivo.getArPath());
            if (!deletedInSftp) {
                log.warn("[DELETE-FILE] No se pudo eliminar el archivo físico en SFTP ('{}'), pero se procederá a remover el registro en BD",
                        archivo.getArPath());
            }

            asArchivoRepository.delete(archivo);
            log.info("[DELETE-FILE] Archivo ID {} eliminado de la base de datos (Removido en SFTP: {})", archivoId, deletedInSftp);
            return true;
        }).orElseGet(() -> {
            log.warn("[DELETE-FILE] No se pudo eliminar: Archivo ID {} no existe en la BD", archivoId);
            return false;
        });
    }

    private static String calculateSha256(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new BufferedInputStream(file.getInputStream(), BUFFER_SIZE)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return bytesToHex(digest.digest());
    }

    private static String extractExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0 && dotIndex < filename.length() - 1) ? filename.substring(dotIndex + 1) : "";
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}