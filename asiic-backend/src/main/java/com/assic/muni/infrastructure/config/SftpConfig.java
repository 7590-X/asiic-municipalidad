package com.assic.muni.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SftpConfig {

    private final SftpProperties sftpProperties;
    private final ResourceLoader resourceLoader;

    @Bean
    public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpProperties.getHost());
        factory.setPort(sftpProperties.getPort());
        factory.setUser(sftpProperties.getUser());
        factory.setAllowUnknownKeys(sftpProperties.isAllowUnknownKeys());

        if (StringUtils.hasText(sftpProperties.getPrivateKeyPath())) {
            Resource privateKeyResource = resourceLoader.getResource(sftpProperties.getPrivateKeyPath());
            if (privateKeyResource.exists()) {
                log.info("Cargando clave privada SFTP desde: {}", sftpProperties.getPrivateKeyPath());
                factory.setPrivateKey(privateKeyResource);
                if (StringUtils.hasText(sftpProperties.getPrivateKeyPassphrase())) {
                    factory.setPrivateKeyPassphrase(sftpProperties.getPrivateKeyPassphrase());
                }
            } else {
                log.warn("El recurso de clave privada SFTP '{}' no fue encontrado. Verificando autenticación por contraseña.",
                        sftpProperties.getPrivateKeyPath());
            }
        }

        if (StringUtils.hasText(sftpProperties.getPassword())) {
            factory.setPassword(sftpProperties.getPassword());
        }

        CachingSessionFactory<SftpClient.DirEntry> cachingSessionFactory =
                new CachingSessionFactory<>(factory, sftpProperties.getSessionCacheSize());
        cachingSessionFactory.setSessionWaitTimeout(sftpProperties.getSessionWaitTimeout());
        cachingSessionFactory.setTestSession(false);

        return cachingSessionFactory;
    }

    @Bean
    public SftpRemoteFileTemplate sftpRemoteFileTemplate(SessionFactory<SftpClient.DirEntry> sftpSessionFactory) {
        SftpRemoteFileTemplate template = new SftpRemoteFileTemplate(sftpSessionFactory);
        template.setRemoteFileSeparator("/");
        return template;
    }
}
