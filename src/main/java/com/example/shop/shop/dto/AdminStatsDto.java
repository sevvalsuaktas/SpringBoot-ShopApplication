package com.example.shop.shop.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminStatsDto { // Sistem istatistiklerini (sayısal verileri) taşımak için kullanılır.
    private long userCount;
    private long productCount;
    private long orderCount;
    private double totalRevenue;
}
