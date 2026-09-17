package com.assic.muni.presentation.api.otrs;

import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleServiceException(
            ServiceException e, HttpServletRequest request) {
        return ResponseEntity.status(e.getHttpStatus().value())
                .body(new ApiResponseDto<>(e.getHttpStatus().value(), request.getRequestURI(),
                        ZonedDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleInfrastructureException(
            InfrastructureException e, HttpServletRequest request) {
        log.error("[INFRASTRUCTURE_ERROR] URI: {} - Error: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(e.getStatus().value())
                .body(new ApiResponseDto<>(e.getStatus().value(), request.getRequestURI(),
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new ApiResponseDto<>(HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                        ZonedDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new ApiResponseDto<>(HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                        ZonedDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleJwtException(
            JwtException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new ApiResponseDto<>(HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                        ZonedDateTime.now(), e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(
            Exception e, HttpServletRequest request) {
        log.error("[UNEXPECTED_ERROR] Error no controlado en {}", request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new ApiResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI(),
                        ZonedDateTime.now(), "Ocurrió un error interno en el servidor. Por favor contacte con soporte.", null));
    }
}