package com.example.shop.shop.controller;

import com.example.shop.shop.dto.PaymentRequestDto;
import com.example.shop.shop.dto.PaymentResponseDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Loggable
    @PostMapping
    public ResponseEntity<PaymentResponseDto> pay(@Valid @RequestBody PaymentRequestDto request) {
        PaymentResponseDto response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}