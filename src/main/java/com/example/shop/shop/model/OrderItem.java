package com.example.shop.shop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) // bir orderItem bir order a aittir
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY) // her orderitem bir product içerir
    @JoinColumn(name = "product_id")
    private Product product;
    private Integer quantity;
    private Double priceAtPurchase; // sipariş anındaki fiyatı
}