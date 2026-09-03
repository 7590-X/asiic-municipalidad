package com.assic.muni.presentation.advice;

import com.assic.muni.application.cqrs.dto.ApiResponseDto;
import com.assic.muni.application.exception.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.ZonedDateTime;

@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleServiceException(ServiceException ex) {
        return ResponseEntity.status(ex.getHttpStatus().value())
                .body(new ApiResponseDto<Object>(
                        ex.getHttpStatus().value(),
                        ex.getHttpStatus().getReasonPhrase(),
                        ZonedDateTime.now(),
                        ex.getMessage(), null
                ));
    }
}
