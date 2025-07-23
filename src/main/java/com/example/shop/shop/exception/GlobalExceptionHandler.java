package com.example.shop.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice // @RestControllerAdvice tüm controller’ların dışındaki ExceptionHandler’ları devreye alır.
public class GlobalExceptionHandler {

    // 1) RuntimeException ve alt tipleri
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex) {
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(error.getStatus())
                .body(error);
    }

    // 2) @Validated ile giren request body validasyonu hataları
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        // join tüm alan hatalarını tek bir mesajda topla
        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse(ex.getMessage());

        ApiError error = ApiError.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .message(msg)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(error.getStatus())
                .body(error);
    }
}

