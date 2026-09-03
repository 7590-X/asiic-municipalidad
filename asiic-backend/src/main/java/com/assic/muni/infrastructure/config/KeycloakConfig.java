package com.assic.muni.infrastructure.config;

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

  @Value("${keycloak.clients.auth.id}")
  private String id;

  @Value("${keycloak.clients.auth.secret}")
  private String secret;

  @Bean
  public Keycloak setupKeycloakAdmin() {
    return KeycloakBuilder.builder()
        .serverUrl(server)
        .realm(realm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(id)
        .clientSecret(secret)
        .build();
  }
}
