package com.example.shop.shop.dto;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto implements Serializable { // kategori bilgilerini tutan dto
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
}
