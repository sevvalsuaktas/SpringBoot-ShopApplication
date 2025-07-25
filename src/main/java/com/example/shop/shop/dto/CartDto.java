package com.example.shop.shop.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto { // sepet bilgilerini tutan dto
    private Long id;
    private Long customerId;
    private List<CartItemDto> items;
    private String status;
}
