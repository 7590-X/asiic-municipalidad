package com.assic.muni.presentation.api.pubs;

import com.assic.muni.application.cqrs.cmd.LoginCmd;
import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.application.cqrs.handler.ConfirmarCuentaCmdHandler;
import com.assic.muni.application.cqrs.handler.LoginCmdHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

    @Mock
    private ConfirmarCuentaCmdHandler confirmarCuentaCmdHandler;

    @Mock
    private LoginCmdHandler loginCmdHandler;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("Debe responder 200 OK y el TokenDto en el payload al hacer login exitoso")
    void debeRetornarOkConTokenAlIniciarSesion() {
        LoginCmd cmd = new LoginCmd("2998123450101", "Password123!");
        TokenDto expectedToken = new TokenDto("access_abc", "refresh_xyz", 300L, 1800L, "Bearer");

        when(loginCmdHandler.handle(cmd)).thenReturn(expectedToken);

        ResponseEntity<ApiResponseDto<TokenDto>> response = authController.login(cmd);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getBody().message()).isEqualTo("Inicio de sesión exitoso");
        assertThat(response.getBody().payload()).isEqualTo(expectedToken);

        verify(loginCmdHandler).handle(cmd);
    }
}
