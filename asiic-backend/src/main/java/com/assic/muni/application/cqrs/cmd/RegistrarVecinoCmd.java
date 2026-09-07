package com.assic.muni.application.cqrs.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistrarVecinoCmd {

    @NotBlank
    @Pattern(regexp = "\\d{13}", message = "Formato de CUI es invalido")
    @JsonProperty("cui")
    private String cui;

    @Size(max = 13)
    @Pattern(regexp = "\\d*[a-zA-Z]?$", message = "Formato de NIT es invalido")
    @JsonProperty("nit")
    private String nit;

    @Size(max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Formato de pasaporte es invalido")
    @JsonProperty("pasaporte")
    private String pasaporte;

    @NotBlank @Size(max = 45)
    @JsonProperty("nombres")
    private String nombres;

    @NotBlank @Size(max = 45)
    @JsonProperty("apellidos")
    private String apellidos;

    @NotBlank
    @Pattern(regexp = "[MF]", message = "Género inválido (M o F)")
    @JsonProperty("genero")
    private String genero;

    @NotBlank
    @Pattern(regexp = "\\d{8,15}", message = "El teléfono debe contener entre 8 y 15 dígitos")
    @JsonProperty("telefono")
    private String telefono;

    @NotBlank
    @Size(max = 45, message = "La longitud del correo sobrepasa los 45 caracteres")
    @Email(message = "El formato del correo es inválido")
    @JsonProperty("correo")
    private String correo;

    @NotBlank
    @Size(max = 100)
    @JsonProperty("direccion")
    private String direccion;

    @NotNull
    @Positive
    @JsonProperty("estado_civil_id")
    private Short estadoCivilId;

    @NotNull
    @Positive
    @JsonProperty("profesion_id")
    private Short profesionId;

    @NotNull
    @Positive
    @JsonProperty("locacion_id")
    private Integer locacionId;
}