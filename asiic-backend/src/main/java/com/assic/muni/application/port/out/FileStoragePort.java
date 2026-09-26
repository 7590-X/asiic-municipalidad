package com.assic.muni.application.port.out;

import org.springframework.web.multipart.MultipartFile;
import com.assic.muni.domain.model.AsArchivo;

public interface FileStoragePort {
    /**
     * Guarda un archivo en el servidor y retorna la entidad AsArchivo guardada.
     */
    AsArchivo storeFile(MultipartFile file);

    /**
     * Obtiene el contenido binario del archivo a partir de su ID registrado.
     *
     * @param archivoId Identificador del archivo en la base de datos.
     * @return Arreglo de bytes con el contenido del archivo.
     */
    byte[] getFile(Integer archivoId);

    /**
     * Elimina el archivo física y lógicamente.
     *
     * @param archivoId Identificador del archivo a eliminar.
     * @return true si se eliminó exitosamente, false si no se encontró.
     */
    boolean deleteFile(Integer archivoId);
}
