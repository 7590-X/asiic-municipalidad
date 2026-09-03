package com.assic.muni.application.config;

import jakarta.ws.rs.client.Client;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

  @Value("${keycloak.server}")
  private String server;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.clients.admin.id}")
  private String id;

  @Value("${keycloak.clients.admin.secret}")
  private String secret;

  @Value("${keycloak.ssl.disable-trust-manager:false}")
  private boolean disableTrustManager;

  @Bean
  public Keycloak setupKeycloakAdmin() {

    KeycloakBuilder builder = KeycloakBuilder.builder()
            .serverUrl(server)
            .realm(realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(id)
            .clientSecret(secret);

    // Solo apagamos la seguridad si estamos en local
    if (disableTrustManager) {
      Client resteasyClient = new ResteasyClientBuilderImpl()
              .disableTrustManager()
              .build();
      builder.resteasyClient(resteasyClient);
    }

    return builder.build();
  }
}
