package com.assic.muni.infrastructure.enums;

import lombok.Getter;

@Getter
public enum KCRole {
    ROLE_VECINO("role_vecino"),

    SYS_ANALISTA("sys_analista"),

    SYS_AGENTE("sys_agente_campo"),

    SYS_ADMIN("sys_admin");

    private final String value;

    KCRole(String value) {
        this.value = value;
    }

    public static KCRole fromValue(String value) {
        for (KCRole role : KCRole.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return null;
    }

    public static boolean isValid(String value) {
        for (KCRole role : KCRole.values()) {
            if (role.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static KCRole fromValueIgnoreCase(String value) {
        for (KCRole role : KCRole.values()) {
            if (role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

    public static boolean isValidIgnoreCase(String value) {
        for (KCRole role : KCRole.values()) {
            if (role.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
