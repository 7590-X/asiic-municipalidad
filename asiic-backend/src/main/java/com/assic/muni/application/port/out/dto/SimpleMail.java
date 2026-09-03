package com.assic.muni.application.port.out.dto;

import jakarta.validation.constraints.NotBlank;

public record SimpleMail(

  @NotBlank(message = "El correo destinatario es obligatorio")
  String destination,

  @NotBlank(message = "El asunto del correo es obligatorio")
  String subject,

  @NotBlank(message = "El cuerpo del correo es obligatorio")
  String htmlBody
) {
}
