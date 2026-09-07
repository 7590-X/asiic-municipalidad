package com.assic.muni.application.cqrs.enums;

public enum ENumero {
    N_CUI("cui"), N_NIT("nit"), N_CORREO("correo");

    private final String value;

    ENumero(String value) {
        this.value = value;
    }

    public static ENumero fromValue(String value) {
        for (ENumero c : ENumero.values()) {
            if(c.value.equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("El número " + value + " no está definido.");
    }
}
