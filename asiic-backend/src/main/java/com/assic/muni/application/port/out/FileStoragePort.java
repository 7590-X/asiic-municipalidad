package com.assic.muni.application.port.out;

import org.springframework.web.multipart.MultipartFile;
import com.assic.muni.domain.model.AsArchivo;

public interface FileStoragePort {
    /**
     * Guarda un archivo en el servidor y retorna la entidad AsArchivo guardada.
     */
    AsArchivo storeFile(MultipartFile file);
}
