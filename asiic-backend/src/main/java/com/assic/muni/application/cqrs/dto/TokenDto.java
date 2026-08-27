package com.assic.muni.application.cqrs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenDto(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") Long expiresIn,
    @JsonProperty("refresh_expires_in") Long refreshExpiresIn,
    @JsonProperty("token_type") String tokenType) {
}
