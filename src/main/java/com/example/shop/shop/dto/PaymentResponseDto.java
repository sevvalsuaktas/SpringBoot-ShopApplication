package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto { // ödemenin repsonse ının tutulduğu dto
    private Long paymentId;
    private String status;
    private String message;
}
