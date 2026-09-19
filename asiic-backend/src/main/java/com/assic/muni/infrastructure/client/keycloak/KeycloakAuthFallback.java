package com.assic.muni.infrastructure.client.keycloak;

import com.assic.muni.application.cqrs.dto.TokenDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class KeycloakAuthFallback implements FallbackFactory<KeycloakAuthClient> {

  @Override
  public KeycloakAuthClient create(Throwable cause) {
    return new KeycloakAuthClient() {
      @Override
      public TokenDto authenticate(MultiValueMap<String, ?> formData) {
        return null;
      }
    };
  }
    
}
