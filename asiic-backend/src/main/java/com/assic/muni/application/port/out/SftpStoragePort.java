package com.assic.muni.application.port.out;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * Puerto de salida para operaciones de almacenamiento remoto en servidor SFTP.
 * Desacoplado de la tecnología de transporte y de la persistencia de base de datos.
 */
public interface SftpStoragePort {

    /**
     * Sube un archivo a partir de un InputStream al servidor SFTP.
     *
     * @param inputStream    Flujo de datos del archivo a subir.
     * @param remoteFilename Nombre del archivo con el que se guardará en el SFTP.
     * @param subDirectory   Subdirectorio relativo al directorio raíz remoto (puede ser nulo o vacío).
     * @return Ruta completa del archivo almacenado en el SFTP.
     */
    String uploadFile(InputStream inputStream, String remoteFilename, String subDirectory);

    /**
     * Sube un archivo a partir de un arreglo de bytes al servidor SFTP.
     *
     * @param data           Contenido en bytes del archivo.
     * @param remoteFilename Nombre del archivo remoto.
     * @param subDirectory   Subdirectorio relativo al directorio raíz remoto.
     * @return Ruta completa del archivo almacenado en el SFTP.
     */
    String uploadFile(byte[] data, String remoteFilename, String subDirectory);

    /**
     * Sube un archivo de tipo MultipartFile al servidor SFTP.
     * Genera un identificador único para el nombre si es necesario o utiliza el original sanitizado.
     *
     * @param file         Archivo MultipartFile proveniente de una petición HTTP.
     * @param subDirectory Subdirectorio relativo al directorio raíz remoto.
     * @return Ruta completa del archivo almacenado en el SFTP.
     */
    String uploadFile(MultipartFile file, String subDirectory);

    /**
     * Descarga el contenido completo de un archivo remoto como arreglo de bytes.
     *
     * @param remoteFilePath Ruta completa o relativa del archivo en el servidor SFTP.
     * @return Arreglo de bytes con el contenido del archivo.
     */
    byte[] downloadFile(String remoteFilePath);

    /**
     * Descarga el contenido de un archivo remoto como un flujo de lectura InputStream.
     *
     * @param remoteFilePath Ruta del archivo en el servidor SFTP.
     * @return Flujo de datos InputStream del archivo remoto.
     */
    InputStream downloadFileInputStream(String remoteFilePath);

    /**
     * Elimina un archivo del servidor SFTP.
     *
     * @param remoteFilePath Ruta del archivo a eliminar en el servidor SFTP.
     * @return true si el archivo fue eliminado exitosamente; false si no existía o no se pudo eliminar.
     */
    boolean deleteFile(String remoteFilePath);

    /**
     * Verifica la existencia de un archivo en el servidor SFTP.
     *
     * @param remoteFilePath Ruta del archivo en el servidor SFTP.
     * @return true si el archivo existe, false en caso contrario.
     */
    boolean exists(String remoteFilePath);

    /**
     * Lista los nombres de archivos presentes en un subdirectorio remoto del SFTP.
     *
     * @param subDirectory Subdirectorio relativo a la raíz remota (o nulo para listar la raíz).
     * @return Lista de nombres de archivos encontrados.
     */
    List<String> listFiles(String subDirectory);
}
