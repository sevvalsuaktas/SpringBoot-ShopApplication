package com.example.shop.shop.service;

import com.example.shop.shop.dto.CartDto;
import com.example.shop.shop.dto.CartItemDto;
import com.example.shop.shop.dto.OrderDto;

public interface CartService {
    // Aktif (ACTIVE) sepeti getirir; yoksa yenisini yaratır
    CartDto getActiveCart(Long customerId);

    // Sepete bir ürün ekler veya miktarı arttırır
    CartItemDto addItem(Long customerId, CartItemDto dto);

    // Sepet öğesini siler
    void removeItem(Long customerId, Long cartItemId);

    // Sepeti ORDERED durumuna getirir (checkout)
    OrderDto checkout(Long customerId);
}
