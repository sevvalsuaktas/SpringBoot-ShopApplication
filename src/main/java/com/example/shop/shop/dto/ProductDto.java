package com.example.shop.shop.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    @NotNull @Positive
    private Double price;
    @NotNull
    private Long categoryId;
    private Boolean inStock;
}