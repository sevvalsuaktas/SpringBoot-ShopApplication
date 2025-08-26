package com.example.shop.shop.controller;

import com.example.shop.shop.dto.CartDto;
import com.example.shop.shop.dto.CartItemDto;
import com.example.shop.shop.dto.OrderDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.service.CartService;
import com.example.shop.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    @Loggable
    @GetMapping("/{customerId}") // customer id sine göre active sepeti döndürür eğer sepet yoksa yeni oluşturur
    public ResponseEntity<CartDto> getActive(@PathVariable Long customerId) {
        CartDto cart = cartService.getActiveCart(customerId);
        return ResponseEntity.ok(cart);
    }

    @Loggable
    @PostMapping("/{customerId}/items") // sepete ürün ekler beya miktarını artırır Body: { "productId": 5, "quantity": 2 }
    public ResponseEntity<CartItemDto> addItem(
            @PathVariable Long customerId,
            @RequestBody CartItemDto dto) {
        CartItemDto added = cartService.addItem(customerId, dto);
        return ResponseEntity.status(201).body(added); // eklendiğine dair repsonse döner
    }

    @Loggable
    @DeleteMapping("/{customerId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long customerId, @PathVariable Long itemId) {
        cartService.removeItem(customerId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Loggable
    @PostMapping("/{customerId}/checkout")
    public ResponseEntity<OrderDto> checkout(@PathVariable Long customerId) {
        OrderDto order = orderService.createFromCart(customerId);
        return ResponseEntity.status(201).body(order);
    }
}