package com.assic.muni.application.port.out.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimpleMail - Pruebas de Validación")
class SimpleMailTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  @DisplayName("Debe ser válido cuando todos los campos requeridos están presentes")
  void shouldBeValidWhenAllFieldsArePresent() {
    SimpleMail mail = new SimpleMail(
        "destinatario@correo.com",
        "Asunto importante",
        "<p>Mensaje con contenido</p>"
    );

    Set<ConstraintViolation<SimpleMail>> violations = validator.validate(mail);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("Debe fallar la validación cuando el destinatario es nulo, vacío o en blanco")
  void shouldFailWhenDestinationIsBlank(String invalidDestination) {
    SimpleMail mail = new SimpleMail(invalidDestination, "Asunto", "Cuerpo");

    Set<ConstraintViolation<SimpleMail>> violations = validator.validate(mail);

    assertThat(violations)
        .hasSize(1)
        .anyMatch(v -> v.getPropertyPath().toString().equals("destination")
            && v.getMessage().equals("El correo destinatario es obligatorio"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("Debe fallar la validación cuando el asunto es nulo, vacío o en blanco")
  void shouldFailWhenSubjectIsBlank(String invalidSubject) {
    SimpleMail mail = new SimpleMail("correo@test.com", invalidSubject, "Cuerpo");

    Set<ConstraintViolation<SimpleMail>> violations = validator.validate(mail);

    assertThat(violations)
        .hasSize(1)
        .anyMatch(v -> v.getPropertyPath().toString().equals("subject")
            && v.getMessage().equals("El asunto del correo es obligatorio"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("Debe fallar la validación cuando el cuerpo es nulo, vacío o en blanco")
  void shouldFailWhenHtmlBodyIsBlank(String invalidBody) {
    SimpleMail mail = new SimpleMail("correo@test.com", "Asunto", invalidBody);

    Set<ConstraintViolation<SimpleMail>> violations = validator.validate(mail);

    assertThat(violations)
        .hasSize(1)
        .anyMatch(v -> v.getPropertyPath().toString().equals("htmlBody")
            && v.getMessage().equals("El cuerpo del correo es obligatorio"));
  }
}
