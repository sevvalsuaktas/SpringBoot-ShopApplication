package com.example.shop.shop.repository;

import com.example.shop.shop.model.Cart;
import com.example.shop.shop.model.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerIdAndStatus(Long customerId, CartStatus status); // findByCustomerIdAndStatus metodu aktif (ACTIVE) sepeti bulmak için, yoksa yenisini oluşturmamız için gerekecek.
}

