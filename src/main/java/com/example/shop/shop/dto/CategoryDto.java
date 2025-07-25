package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto { // kategori bilgilerini tutan dto
    private Long id;
    private String name;
    private String description;
}
