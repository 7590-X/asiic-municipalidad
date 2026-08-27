package com.assic.muni.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public class ServiceException extends RuntimeException {

  @Getter
  private final HttpStatus httpStatus;

  public ServiceException(HttpStatus httpStatus, String message) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
