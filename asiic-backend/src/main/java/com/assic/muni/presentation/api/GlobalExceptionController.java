package com.assic.muni.presentation.api;

import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleServiceException(
            ServiceException e, HttpServletRequest request) {
        return ResponseEntity.status(e.getHttpStatus().value())
                .body(new ApiResponseDto<>(e.getHttpStatus().value(), request.getRequestURI(),
                        ZonedDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleValidation(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new ApiResponseDto<>(HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                        ZonedDateTime.now(), detalle, null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new ApiResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI(),
                        ZonedDateTime.now(), e.getMessage(), null));
    }
}