package com.assic.muni.domain.event;

public record VecinoCreadoEvent(
    String userId, String email, String fullName) {
}
