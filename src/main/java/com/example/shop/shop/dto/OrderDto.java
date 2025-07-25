package com.example.shop.shop.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto { // sipariş detayını tutan dto
    private Long id;
    private Long customerId;
    private List<OrderItemDto> items;
    private String status;
    private LocalDateTime createdAt; // oluşturulduğu saat
    private LocalDateTime updatedAt;
}
