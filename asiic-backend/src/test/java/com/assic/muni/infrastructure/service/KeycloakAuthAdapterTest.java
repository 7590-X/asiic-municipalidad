package com.assic.muni.infrastructure.service;

import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.infrastructure.client.keycloak.KeycloakAuthClient;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KeycloakAuthAdapter - Pruebas Unitarias")
class KeycloakAuthAdapterTest {

    @Mock
    private KeycloakAuthClient keycloakAuthClient;

    @InjectMocks
    private KeycloakAuthAdapter keycloakAuthAdapter;

    @Captor
    private ArgumentCaptor<MultiValueMap<String, String>> formCaptor;

    private static final String CLIENT_ID = "test-auth-client";
    private static final String CLIENT_SECRET = "test-auth-secret";
    private static final String USERNAME = "2998123450101";
    private static final String PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakAuthAdapter, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(keycloakAuthAdapter, "clientSecret", CLIENT_SECRET);
    }

    @Nested
    @DisplayName("Escenarios de Éxito")
    class ExitoTests {

        @Test
        @DisplayName("Debe autenticar correctamente y enviar los parámetros requeridos a Keycloak")
        void debeAutenticarExitosamente() {
            TokenDto tokenMock = new TokenDto("token-abc", "refresh-xyz", 300L, 1800L, "Bearer");
            when(keycloakAuthClient.authenticate(any())).thenReturn(tokenMock);

            TokenDto result = keycloakAuthAdapter.authenticate(USERNAME, PASSWORD);

            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("token-abc");
            assertThat(result.refreshToken()).isEqualTo("refresh-xyz");

            verify(keycloakAuthClient).authenticate(formCaptor.capture());
            MultiValueMap<String, String> capturedForm = formCaptor.getValue();
            assertThat(capturedForm.getFirst("grant_type")).isEqualTo("password");
            assertThat(capturedForm.getFirst("client_id")).isEqualTo(CLIENT_ID);
            assertThat(capturedForm.getFirst("client_secret")).isEqualTo(CLIENT_SECRET);
            assertThat(capturedForm.getFirst("username")).isEqualTo(USERNAME);
            assertThat(capturedForm.getFirst("password")).isEqualTo(PASSWORD);
            assertThat(capturedForm.getFirst("scope")).isEqualTo("openid");
        }
    }

    @Nested
    @DisplayName("Escenarios de Error")
    class ErrorTests {

        @Test
        @DisplayName("Debe lanzar ServiceException con UNAUTHORIZED cuando Keycloak responde 401 Unauthorized")
        void debeLanzarServiceExceptionPorUnauthorized() {
            Request request = Request.create(Request.HttpMethod.POST, "/token", Collections.emptyMap(), null, new RequestTemplate());
            FeignException.Unauthorized unauthorizedException = new FeignException.Unauthorized("Invalid credentials", request, null, null);

            when(keycloakAuthClient.authenticate(any())).thenThrow(unauthorizedException);

            assertThatThrownBy(() -> keycloakAuthAdapter.authenticate(USERNAME, PASSWORD))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("Credenciales incorrectas")
                    .extracting(e -> ((ServiceException) e).getHttpStatus())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Debe lanzar ServiceException con UNAUTHORIZED cuando Keycloak responde 400 Bad Request (invalid_grant)")
        void debeLanzarServiceExceptionPorBadRequest() {
            Request request = Request.create(Request.HttpMethod.POST, "/token", Collections.emptyMap(), null, new RequestTemplate());
            FeignException.BadRequest badRequestException = new FeignException.BadRequest("Invalid user credentials", request, null, null);

            when(keycloakAuthClient.authenticate(any())).thenThrow(badRequestException);

            assertThatThrownBy(() -> keycloakAuthAdapter.authenticate(USERNAME, PASSWORD))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("Credenciales incorrectas")
                    .extracting(e -> ((ServiceException) e).getHttpStatus())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Debe lanzar ServiceException cuando Keycloak responde un TokenDto con access_token nulo")
        void debeLanzarServiceExceptionCuandoTokenEsNulo() {
            when(keycloakAuthClient.authenticate(any())).thenReturn(new TokenDto(null, null, null, null, null));

            assertThatThrownBy(() -> keycloakAuthAdapter.authenticate(USERNAME, PASSWORD))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("Credenciales incorrectas")
                    .extracting(e -> ((ServiceException) e).getHttpStatus())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Debe lanzar InfrastructureException con SERVICE_UNAVAILABLE cuando falla la conexión con Keycloak")
        void debeLanzarInfrastructureExceptionPorErrorDeConexion() {
            Request request = Request.create(Request.HttpMethod.POST, "/token", Collections.emptyMap(), null, new RequestTemplate());
            FeignException.ServiceUnavailable serviceUnavailable = new FeignException.ServiceUnavailable("Keycloak down", request, null, null);

            when(keycloakAuthClient.authenticate(any())).thenThrow(serviceUnavailable);

            assertThatThrownBy(() -> keycloakAuthAdapter.authenticate(USERNAME, PASSWORD))
                    .isInstanceOf(InfrastructureException.class)
                    .hasMessageContaining("servicio de autenticación no está disponible")
                    .extracting(e -> ((InfrastructureException) e).getStatus())
                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
