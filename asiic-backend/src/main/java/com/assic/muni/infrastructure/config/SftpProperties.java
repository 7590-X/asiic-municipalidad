package com.assic.muni.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sftp")
public class SftpProperties {

    /**
     * Dirección IP o hostname del servidor SFTP.
     */
    private String host;

    /**
     * Puerto del servicio SFTP (por defecto 22).
     */
    private int port = 22;

    /**
     * Nombre de usuario para la autenticación SFTP.
     */
    private String user;

    /**
     * Contraseña del usuario SFTP (opcional si se utiliza clave privada).
     */
    private String password;

    /**
     * Directorio raíz remoto donde se almacenarán los archivos.
     */
    private String remoteDirectory;

    /**
     * Ruta del recurso de la clave privada (ej: classpath:keys/id_rsa o file:/path/to/key).
     */
    private String privateKeyPath;

    /**
     * Frase de paso (passphrase) de la clave privada, si corresponde.
     */
    private String privateKeyPassphrase;

    /**
     * Indica si se permiten claves de host desconocidas (HostKeyChecking desactivado).
     */
    private boolean allowUnknownKeys;

    /**
     * Tamaño máximo del pool de sesiones en CachingSessionFactory.
     */
    private int sessionCacheSize = 10;

    /**
     * Tiempo de espera máximo (en milisegundos) para obtener una sesión del pool.
     */
    private long sessionWaitTimeout = 5000L;
}
