package com.assic.muni.infrastructure.client.keycloak;

import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
public class KeycloakAuthFallback implements FallbackFactory<KeycloakAuthClient> {

  @Override
  public KeycloakAuthClient create(Throwable cause) {
    return new KeycloakAuthClient() {
      @Override
      public TokenDto authenticate(MultiValueMap<String, ?> formData) {
        log.error("[KEYCLOAK_AUTH_FALLBACK] Error al invocar autenticación en Keycloak: {}",
            cause != null ? cause.getMessage() : "Desconocido", cause);
        throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE,
            "El servicio de autenticación no está disponible en este momento.");
      }

      @Override
      public void logout(MultiValueMap<String, ?> formData) {
        log.error("[KEYCLOAK_LOGOUT_FALLBACK] Error al invocar cierre de sesión en Keycloak: {}",
            cause != null ? cause.getMessage() : "Desconocido", cause);
        throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE,
            "El servicio de autenticación no está disponible para procesar el cierre de sesión.");
      }
    };
  }
}
