package com.assic.muni.application.cqrs.dto;

import java.time.ZonedDateTime;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;

public record ApiResponseDto<T>(
    @SerializedName("code") int code,
    @SerializedName("action") String action,
    @SerializedName("datetime") ZonedDateTime dateTime,
    @SerializedName("message") String message,
    @SerializedName("payload") T payload) {
}
