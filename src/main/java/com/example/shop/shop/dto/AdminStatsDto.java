package com.example.shop.shop.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminStatsDto {
    private long userCount;
    private long productCount;
    private long orderCount;
    private double totalRevenue;
}
