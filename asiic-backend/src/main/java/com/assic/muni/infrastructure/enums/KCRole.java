package com.assic.muni.infrastructure.enums;

import lombok.Getter;

@Getter
public enum KCRole {
    ROLE_VECINO("role_vecino");

    private final String value;

    KCRole(String value) {
        this.value = value;
    }
}
