package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto { // sepetin içindeki ürünlerin bilgilerinin tutulduğu dto
    private Long id;
    private Long productId;
    private Integer quantity;
    private Double priceAtPurchase;
}

