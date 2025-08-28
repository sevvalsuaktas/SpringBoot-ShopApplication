package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto { // sipariş bilgileri
    private Long id;
    private Long productId;
    private Integer quantity;
}