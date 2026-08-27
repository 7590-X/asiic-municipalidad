package com.assic.muni.application.cqrs.cmd;

public record RegistrarVecinoCmd(
    String email,
    String username,
    String password,
    String firstName,
    String lastName) {
}
