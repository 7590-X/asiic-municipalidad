package com.assic.muni.domain.event;

public record CuentaCreadaEvent(
    String userId, String email, String fullName) {
}
