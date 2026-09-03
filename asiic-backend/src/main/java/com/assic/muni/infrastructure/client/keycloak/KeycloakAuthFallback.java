package com.assic.muni.infrastructure.client.keycloak;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Component
public class KeycloakAuthFallback implements FallbackFactory<KeycloakAuthClient> {

  @Override
  public KeycloakAuthClient create(Throwable cause) {
    return new KeycloakAuthClient() {
      @Override
      public Map<String, Object> authenticate(MultiValueMap<String, String> formData) {
        return Map.of();
      }
    };
  }
    
}
