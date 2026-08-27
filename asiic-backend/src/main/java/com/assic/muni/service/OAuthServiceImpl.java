package com.assic.muni.service;

import java.util.Collections;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.assic.muni.client.KeycloakAuthClient;
import com.assic.muni.dto.RegisterLoginCommand;
import com.assic.muni.dto.RegisterUserCommand;
import com.assic.muni.exception.ServiceException;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

  private final Keycloak keycloak;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.clients.admin.id}")
  private String clientId;

  @Value("${keycloak.clients.admin.secret}")
  private String secret;

  private final KeycloakAuthClient authClient;

  @Override
  public String register(RegisterUserCommand command) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(command.username());
    user.setEmail(command.email());
    user.setFirstName(command.firstName());
    user.setLastName(command.lastName());
    user.setEnabled(true);

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(command.password());
    credential.setTemporary(false);
    user.setCredentials(Collections.singletonList(credential));

    Response response = keycloak.realm(realm).users().create(user);

    if (response.getStatus() != 201) {
      throw new ServiceException(HttpStatus.valueOf(response.getStatus()), "No se pudo registrar el usuario");
    }
    return "123";

  }

  @Override
  public Map<String, Object> login(RegisterLoginCommand command) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "password");
    formData.add("client_id", clientId);
    formData.add("client_secret", secret);
    formData.add("username", command.username());
    formData.add("password", command.password());
    return authClient.authenticate(formData);
  }
}
