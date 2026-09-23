package com.assic.muni.application.enums;

import lombok.Getter;

@Getter
public enum ECatalogo {
    C_ESTADO_CIVIL("estado-civil"),
    C_PROFESION("profesion"),
    C_DEPENDENCIAS("dependencias"),
    C_TIPOS_SERVICIO("tipos-servicio"),
    C_TIPOS_DENUNCIA("tipos-denuncia"),
    C_AREAS_SUGERENCIA("areas-sugerencia");

    private final String value;


    ECatalogo(String value) {
        this.value = value;
    }

    public static ECatalogo fromValue(String value) {
        for (ECatalogo c : ECatalogo.values()) {
            if (c.getValue().equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Catálogo " + value + " no está definido.");
    }
}
