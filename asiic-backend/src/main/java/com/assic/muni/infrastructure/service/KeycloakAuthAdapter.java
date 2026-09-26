package com.assic.muni.infrastructure.service;

import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.application.port.out.AuthenticationPort;
import com.assic.muni.infrastructure.client.keycloak.KeycloakAuthClient;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthAdapter implements AuthenticationPort {

    private final KeycloakAuthClient keycloakAuthClient;

    @Value("${keycloak.clients.auth.id}")
    private String clientId;

    @Value("${keycloak.clients.auth.secret}")
    private String clientSecret;

    @Override
    public TokenDto authenticate(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", username);
        formData.add("password", password);
        formData.add("scope", "openid");

        try {
            TokenDto tokenResponse = keycloakAuthClient.authenticate(formData);
            if (tokenResponse == null || tokenResponse.accessToken() == null) {
                log.warn("[AUTH_EMPTY_RESPONSE] Respuesta vacía de Keycloak para el usuario: {}", username);
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas. Verifique su usuario y contraseña.");
            }
            return tokenResponse;
        } catch (FeignException.BadRequest | FeignException.Unauthorized e) {
            log.warn("[KEYCLOAK_AUTH_FAILED] Falló autenticación para usuario {}: {}", username, e.getMessage());
            throw new ServiceException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas. Verifique su usuario y contraseña.");
        } catch (FeignException e) {
            log.error("[KEYCLOAK_AUTH_ERROR] Error de comunicación con Keycloak al autenticar usuario {}", username, e);
            throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autenticación no está disponible en este momento.");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[KEYCLOAK_UNEXPECTED_ERROR] Error inesperado durante la autenticación de {}", username, e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado al procesar la autenticación.");
        }
    }

    @Override
    public void logout(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        try {
            keycloakAuthClient.logout(formData);
            log.info("[KEYCLOAK_LOGOUT_SUCCESS] Cierre de sesión exitoso en Keycloak");
        } catch (FeignException.BadRequest e) {
            // El token ya puede haber expirado o haber sido invalidado previamente; tratamos el logout de manera idempotente
            log.warn("[KEYCLOAK_LOGOUT_TOKEN_INVALID] El refresh token es inválido o ya expiró: {}", e.getMessage());
        } catch (FeignException e) {
            log.error("[KEYCLOAK_LOGOUT_ERROR] Error de comunicación con Keycloak durante logout: {}", e.getMessage(), e);
            throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autenticación no está disponible en este momento.");
        } catch (Exception e) {
            log.error("[KEYCLOAK_LOGOUT_UNEXPECTED_ERROR] Error inesperado durante el cierre de sesión", e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado al procesar el cierre de sesión.");
        }
    }

    @Override
    public TokenDto refreshToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        try {
            TokenDto tokenResponse = keycloakAuthClient.authenticate(formData);
            if (tokenResponse == null || tokenResponse.accessToken() == null) {
                log.warn("[REFRESH_EMPTY_RESPONSE] Respuesta vacía de Keycloak al renovar token");
                throw new ServiceException(HttpStatus.UNAUTHORIZED, "La sesión ha expirado o el token es inválido. Por favor, inicie sesión nuevamente.");
            }
            return tokenResponse;
        } catch (FeignException.BadRequest | FeignException.Unauthorized e) {
            log.warn("[KEYCLOAK_REFRESH_FAILED] Falló refresco de token: {}", e.getMessage());
            throw new ServiceException(HttpStatus.UNAUTHORIZED, "La sesión ha expirado o el token es inválido. Por favor, inicie sesión nuevamente.");
        } catch (FeignException e) {
            log.error("[KEYCLOAK_REFRESH_ERROR] Error de comunicación con Keycloak durante refresh token", e);
            throw new InfrastructureException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autenticación no está disponible en este momento.");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[KEYCLOAK_REFRESH_UNEXPECTED_ERROR] Error inesperado durante el refresco de token", e);
            throw new InfrastructureException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado al renovar el token de sesión.");
        }
    }
}
