package com.assic.muni.application.port.out.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public record SimpleMail(

  @NotBlank(message = "El correo destinatario es obligatorio")
  String destination,

  @NotBlank(message = "El asunto del correo es obligatorio")
  String subject,

  @NotBlank(message = "El cuerpo del correo es obligatorio")
  String htmlBody
) {
}
