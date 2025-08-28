package com.example.shop.shop.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private HttpStatus status;
    private String message;
    private LocalDateTime timestamp;
}