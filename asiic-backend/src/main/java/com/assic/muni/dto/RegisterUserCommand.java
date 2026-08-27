package com.assic.muni.dto;

public record RegisterUserCommand(
    String email,
    String username,
    String password,
    String firstName,
    String lastName) {
}
