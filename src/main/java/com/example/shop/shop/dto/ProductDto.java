package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto { // ürünle alakalı bilgilerin tutulduğu dto
    private Long id;
    private String name;
    private String description; // açıklaması yani kategorisinin ismi
    private Double price;
    private String imageUrl;
    private Long categoryId;
    private Boolean inStock; // stokta olup olmama durumu
}
