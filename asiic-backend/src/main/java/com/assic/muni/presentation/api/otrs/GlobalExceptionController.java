package com.assic.muni.presentation.api.otrs;

import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.exception.ServiceException;
import com.assic.muni.infrastructure.exception.InfrastructureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

/**
 * Hereda de {@link ResponseEntityExceptionHandler} para que las excepciones propias de Spring MVC
 * (validación, JSON mal formado, 404, 405, tipos inválidos, etc.) conserven su código HTTP correcto
 * en lugar de caer en el handler genérico como 500.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionController extends ResponseEntityExceptionHandler {

    private static final String MENSAJE_ERROR_INTERNO =
            "Ocurrió un error interno en el servidor. Por favor contacte con soporte.";

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleServiceException(
            ServiceException e, HttpServletRequest request) {
        return buildResponse(e.getHttpStatus(), request.getRequestURI(), e.getMessage());
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInfrastructureException(
            InfrastructureException e, HttpServletRequest request) {
        log.error("[INFRASTRUCTURE_ERROR] URI: {} - Error: {}", request.getRequestURI(), e.getMessage());
        return buildResponse(e.getStatus(), request.getRequestURI(), e.getMessage());
    }

    /** Lanzada por Hibernate Validator al persistir entidades con constraints inválidos. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        String detalle = e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, request.getRequestURI(), detalle);
    }

    /** Sin este handler, el de {@link Exception} convertiría los 403 de @PreAuthorize en 500. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, request.getRequestURI(),
                "No tiene permisos para realizar esta acción");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(
            Exception e, HttpServletRequest request) {
        log.error("[UNEXPECTED_ERROR] Error no controlado en {}", request.getRequestURI(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI(), MENSAJE_ERROR_INTERNO);
    }

    /**
     * Punto común por el que pasan todas las excepciones de Spring MVC; se reemplaza el
     * {@link ProblemDetail} por defecto con el formato {@link ApiResponseDto} de la aplicación.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        String mensaje = switch (ex) {
            case MethodArgumentNotValidException e -> e.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            case TypeMismatchException e -> "Valor inválido para '" + e.getPropertyName() + "': "
                    + NestedExceptionUtils.getMostSpecificCause(e).getMessage();
            default -> body instanceof ProblemDetail pd && pd.getDetail() != null ? pd.getDetail() : ex.getMessage();
        };

        if (statusCode.is5xxServerError()) {
            log.error("[MVC_ERROR] Error en {}", getUri(request), ex);
            mensaje = MENSAJE_ERROR_INTERNO;
        }

        return ResponseEntity.status(statusCode).headers(headers)
                .body(new ApiResponseDto<>(statusCode.value(), getUri(request), ZonedDateTime.now(), mensaje, null));
    }

    private static ResponseEntity<ApiResponseDto<Void>> buildResponse(HttpStatusCode status, String uri, String mensaje) {
        return ResponseEntity.status(status)
                .body(new ApiResponseDto<>(status.value(), uri, ZonedDateTime.now(), mensaje, null));
    }

    private static String getUri(WebRequest request) {
        return request instanceof ServletWebRequest swr
                ? swr.getRequest().getRequestURI()
                : request.getDescription(false);
    }
}
