package com.example.shop.shop.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts") // database de carts isimli tablo oluşturuyor
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id // bu alanın tablonun primary key i olduğunu gösterir
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;

    @OneToMany( // bir cart nesnesi birden fazla cartItem içerebilir
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true // bir cartItem sepetten çıkarıldığında veritabından da silinir
    )

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private CartStatus status; // sepetin durumunu tutar
}

