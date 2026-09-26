package com.assic.muni.application.cqrs.handler;

import com.assic.muni.application.cqrs.cmd.LoginCmd;
import com.assic.muni.application.cqrs.dto.TokenDto;
import com.assic.muni.application.port.out.AuthenticationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginCmdHandler - Pruebas Unitarias")
class LoginCmdHandlerTest {

    @Mock
    private AuthenticationPort authenticationPort;

    @InjectMocks
    private LoginCmdHandler loginCmdHandler;

    @Test
    @DisplayName("Debe autenticar exitosamente y retornar TokenDto")
    void debeAutenticarExitosamente() {
        LoginCmd cmd = new LoginCmd("2998123450101", "Password123!");
        TokenDto expectedToken = new TokenDto("access_123", "refresh_123", 300L, 1800L, "Bearer");

        when(authenticationPort.authenticate(cmd.username(), cmd.password())).thenReturn(expectedToken);

        TokenDto actualToken = loginCmdHandler.handle(cmd);

        assertThat(actualToken).isNotNull();
        assertThat(actualToken.accessToken()).isEqualTo("access_123");
        assertThat(actualToken.refreshToken()).isEqualTo("refresh_123");
        assertThat(actualToken.tokenType()).isEqualTo("Bearer");
        verify(authenticationPort).authenticate(cmd.username(), cmd.password());
    }
}
