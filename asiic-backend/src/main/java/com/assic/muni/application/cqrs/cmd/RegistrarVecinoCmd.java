package com.assic.muni.application.cqrs.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistrarVecinoCmd {

    @NotBlank
    @Pattern(regexp = "\\d{13}", message = "El CUI debe contener 13 dígitos")
    @JsonProperty("cui")
    private String cui;

    @Size(max = 12)
    @Pattern(regexp = "\\d{0,12}", message = "NIT inválido")
    @JsonProperty("nit")
    private String nit;              // RN2: Opcional

    @Size(max = 20)
    @JsonProperty("pasaporte")
    private String pasaporte;        // RN2: Opcional

    @NotBlank @Size(max = 45)
    @JsonProperty("nombres")
    private String nombres;

    @NotBlank @Size(max = 45)
    @JsonProperty("apellidos")
    private String apellidos;

    @NotBlank
    @Pattern(regexp = "[MFmf]", message = "Género inválido (M o F)")
    @JsonProperty("genero")
    private String genero;

    @NotBlank
    @Pattern(regexp = "\\d{8,15}", message = "El teléfono debe contener entre 8 y 15 dígitos")
    @JsonProperty("telefono")
    private String telefono;         // RN2: Opcional

    @NotBlank @Email(message = "El formato del correo es inválido") @Size(max = 45)
    @JsonProperty("correo")
    private String correo;

    @NotBlank @Size(max = 100)
    @JsonProperty("direccion")
    private String direccion;        // Dirección Exacta (residencia)

    @NotNull
    @Positive
    @JsonProperty("estado_civil_id")
    private Short estadoCivilId;

    @NotNull
    @Positive
    @JsonProperty("profesion_id")
    private Short profesionId;

    @NotNull @Positive
    @JsonProperty("locacion_id")
    private Integer locacionId;      // Zona (as_locaciones)
}