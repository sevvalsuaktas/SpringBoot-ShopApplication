package com.example.shop.shop.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;  // order id
    private Long customerId;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private String status;    // NEW / PROCESSING / COMPLETED / CANCELLED
}
