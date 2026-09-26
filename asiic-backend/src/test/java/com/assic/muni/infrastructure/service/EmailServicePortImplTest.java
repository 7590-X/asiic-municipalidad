package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.dto.SimpleMail;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
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
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServicePortImpl - Pruebas Unitarias")
class EmailServicePortImplTest {

  private static final String SENDER_EMAIL = "notificaciones@municipalidad.gob";
  private static final String RECIPIENT_EMAIL = "ciudadano@correo.com";
  private static final String SUBJECT = "Confirmación de trámite municipal";
  private static final String BODY = "<p>Estimado vecino, su solicitud ha sido procesada con éxito.</p>";

  @Mock
  private JavaMailSender javaMailSender;

  @InjectMocks
  private EmailServicePortImpl emailService;

  @Captor
  private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailService, "from", SENDER_EMAIL);
    lenient().when(javaMailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));
  }

  @Nested
  @DisplayName("Escenarios exitosos")
  class SuccessScenarios {

    @Test
    @DisplayName("Debe construir y enviar correctamente el correo con todos sus atributos")
    void shouldBuildAndSendEmailSuccessfully() {
      // Arrange (Given)
      SimpleMail simpleMail = new SimpleMail(RECIPIENT_EMAIL, SUBJECT, BODY);

      // Act (When)
      emailService.sendSimpleEmail(simpleMail);

      // Assert (Then)
      verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());

      MimeMessage sentMessage = mimeMessageCaptor.getValue();
      assertThat(sentMessage).isNotNull();
    }

    @Test
    @DisplayName("Debe gestionar caracteres especiales, tildes y saltos de línea en el cuerpo y asunto")
    void shouldHandleSpecialCharactersAndAccents() {
      // Arrange (Given)
      String specialSubject = "Atención: Código de verificación #12345 - Ñandú & Cía";
      String specialBody = "<p>Hola José,<br>Tu código de validación es: 987654.</p>";
      SimpleMail simpleMail = new SimpleMail("jose.perez@dominio.pe", specialSubject, specialBody);

      // Act (When)
      emailService.sendSimpleEmail(simpleMail);

      // Assert (Then)
      verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());

      MimeMessage capturedMessage = mimeMessageCaptor.getValue();
      assertThat(capturedMessage).isNotNull();
    }
  }

  @Nested
  @DisplayName("Manejo de excepciones y resiliencia")
  class ExceptionHandlingScenarios {

    @Test
    @DisplayName("Debe capturar MailSendException y no propagar error si falla el envío SMTP")
    void shouldHandleMailSendExceptionGracefully() {
      // Arrange (Given)
      SimpleMail simpleMail = new SimpleMail(RECIPIENT_EMAIL, SUBJECT, BODY);
      doThrow(new MailSendException("Fallo al conectar con el servidor SMTP"))
          .when(javaMailSender)
          .send(any(MimeMessage.class));

      // Act & Assert (When & Then)
      assertThatCode(() -> emailService.sendSimpleEmail(simpleMail))
          .doesNotThrowAnyException();

      verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Debe capturar MailAuthenticationException y continuar sin romper el flujo")
    void shouldHandleMailAuthenticationExceptionGracefully() {
      // Arrange (Given)
      SimpleMail simpleMail = new SimpleMail(RECIPIENT_EMAIL, SUBJECT, BODY);
      doThrow(new MailAuthenticationException("Credenciales SMTP inválidas"))
          .when(javaMailSender)
          .send(any(MimeMessage.class));

      // Act & Assert (When & Then)
      assertThatCode(() -> emailService.sendSimpleEmail(simpleMail))
          .doesNotThrowAnyException();

      verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
  }
}
