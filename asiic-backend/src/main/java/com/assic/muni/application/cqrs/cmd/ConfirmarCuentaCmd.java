package com.assic.muni.application.cqrs.cmd;

public record ConfirmarCuentaCmd(
        String token,
        String password
) {
}
