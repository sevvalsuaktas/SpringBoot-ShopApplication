package com.example.shop.shop.controller;

import com.example.shop.shop.dto.CartDto;
import com.example.shop.shop.dto.CartItemDto;
import com.example.shop.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Aktif sepeti getirir (yoksa yeni yaratır)
     * GET /api/v1/cart/{customerId}
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CartDto> getActive(@PathVariable Long customerId) {
        CartDto cart = cartService.getActiveCart(customerId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Sepete ürün ekler veya miktarını artırır
     * POST /api/v1/cart/{customerId}/items
     * Body: { "productId": 5, "quantity": 2 }
     */
    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartItemDto> addItem(
            @PathVariable Long customerId,
            @RequestBody CartItemDto dto) {
        CartItemDto added = cartService.addItem(customerId, dto);
        return ResponseEntity.status(201).body(added);
    }

    /**
     * Sepet öğesini siler
     * DELETE /api/v1/cart/items/{itemId}
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Sepeti ORDERED durumuna çevirir (checkout)
     * POST /api/v1/cart/{customerId}/checkout
     */
    @PostMapping("/{customerId}/checkout")
    public ResponseEntity<CartDto> checkout(@PathVariable Long customerId) {
        CartDto checked = cartService.checkout(customerId);
        return ResponseEntity.ok(checked);
    }
}



