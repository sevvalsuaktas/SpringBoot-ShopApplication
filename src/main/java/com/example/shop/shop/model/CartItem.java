package com.example.shop.shop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // bir cartItem bir cart a ait
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY) // bir cartItem bir product içerir
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity; // sepete eklenen ürün adedini saklıyor
}
