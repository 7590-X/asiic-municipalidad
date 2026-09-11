package com.assic.muni.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class InfrastructureException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public InfrastructureException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
