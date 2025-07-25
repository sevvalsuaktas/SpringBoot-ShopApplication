package com.example.shop.shop.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) // bir order birden çok orderItem içerir
    @Builder.Default
    private List<OrderItem> items = List.of();

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // siparişin durumunu tutar

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
