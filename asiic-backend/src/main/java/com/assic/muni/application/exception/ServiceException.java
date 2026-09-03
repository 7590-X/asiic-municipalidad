package com.assic.muni.application.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public class ServiceException extends RuntimeException {

  @Getter
  private final HttpStatus httpStatus;

  public ServiceException(HttpStatus httpStatus, String message) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
