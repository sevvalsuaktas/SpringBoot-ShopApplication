package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto { // sepetin içindeki ürünün bilgilerini tutan dto
    private Long id;
    private Long productId;
    private Integer quantity;
}

