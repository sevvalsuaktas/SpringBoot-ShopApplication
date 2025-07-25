package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDto { // stok bilgisini tutan dto
    private Long productId;
    private int available;
}
