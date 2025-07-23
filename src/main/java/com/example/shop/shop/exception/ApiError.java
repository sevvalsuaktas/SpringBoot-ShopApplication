package com.example.shop.shop.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiError {
    private HttpStatus status; // http durum kodu
    private String message; // döndürülen mesaj
    private LocalDateTime timestamp; // hata zamanını tutuyor
}
