package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto { // ödeme bilgilerinin alındığı dto
    private Long orderId;
    private Double amount;
    private String method; // ödeme şekli
}