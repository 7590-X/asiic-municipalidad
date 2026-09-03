package com.assic.muni.infrastructure.client.keycloak;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "keycloak-client", url = "${keycloak.server}/realms/${keycloak.realm}/protocol/openid-connect")
public interface KeycloakAuthClient {

  @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  Map<String, Object> authenticate(@RequestBody MultiValueMap<String, String> formData);
}