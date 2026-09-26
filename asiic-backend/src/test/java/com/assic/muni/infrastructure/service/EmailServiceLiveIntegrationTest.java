package com.assic.muni.infrastructure.service;

import com.assic.muni.application.port.out.dto.SimpleMail;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(classes = {
    MailSenderAutoConfiguration.class,
    EmailServicePortImpl.class
})
@Tag("live-test")
@EnabledIfEnvironmentVariable(
    named = "MAIL_HOST",
    matches = ".+",
    disabledReason = "Omitido: Se requiere la variable de entorno MAIL_HOST para ejecutar el envío real en el servidor"
)
@DisplayName("EmailServicePortImpl - Prueba de Integración Real (Variables de Entorno)")
class EmailServiceLiveIntegrationTest {

  @Autowired
  private EmailServicePortImpl emailService;

  @Autowired
  private JavaMailSender javaMailSender;

  @Test
  @DisplayName("Debe conectarse exitosamente al servidor SMTP con las variables de entorno del sistema")
  void shouldConnectToSmtpServerSuccessfully() throws MessagingException {
    assertThat(javaMailSender).isInstanceOf(JavaMailSenderImpl.class);
    JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) javaMailSender;

    // Valida handshake TLS y autenticación real con el servidor SMTP configurado en el entorno
    assertThatCode(mailSenderImpl::testConnection)
        .as("La conexión y autenticación con el servidor SMTP debe ser exitosa")
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Debe enviar un correo electrónico real usando las variables de entorno")
  void shouldSendRealEmailUsingEnvironmentVariables() {
    String recipient = System.getenv("TEST_EMAIL_TO");
    if (recipient == null || recipient.isBlank()) {
      recipient = emailService.from; // Por defecto envía al propio remitente configurado en MAIL_FROM
    }

    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String subject = "Prueba de Integración - Envío de Correo ASIIC [" + timestamp + "]";
    String body = "Hola,\n\nEste es un correo de prueba generado desde el test de integración de ASIIC Backend usando variables de entorno del servidor.\n"
        + "Fecha y hora: " + timestamp + "\n"
        + "Remitente: " + emailService.from + "\n"
        + "Destinatario: " + recipient + "\n\n"
        + "El servicio de correos está funcionando correctamente con las variables del entorno del servidor.";

    SimpleMail simpleMail = new SimpleMail(recipient, subject, body);

    assertThatCode(() -> emailService.sendSimpleEmail(simpleMail))
        .as("El envío del correo real no debe arrojar ninguna excepción")
        .doesNotThrowAnyException();

    System.out.println("==================================================================");
    System.out.println("CORREO ENVIADO EXITOSAMENTE CON VARIABLES DE ENTORNO");
    System.out.println("De: " + emailService.from);
    System.out.println("Para: " + recipient);
    System.out.println("Asunto: " + subject);
    System.out.println("==================================================================");
  }
}
