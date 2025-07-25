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

    @GetMapping("/{customerId}") // customer id sine göre active sepeti döndürür eğer sepet yoksa yeni oluşturur
    public ResponseEntity<CartDto> getActive(@PathVariable Long customerId) {
        CartDto cart = cartService.getActiveCart(customerId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{customerId}/items") // sepete ürün ekler beya miktarını artırır Body: { "productId": 5, "quantity": 2 }
    public ResponseEntity<CartItemDto> addItem(
            @PathVariable Long customerId,
            @RequestBody CartItemDto dto) {
        CartItemDto added = cartService.addItem(customerId, dto);
        return ResponseEntity.status(201).body(added); // eklendiğine dair repsonse döner
    }

    @DeleteMapping("/items/{itemId}") // girilen item id ye göre ürünü sepetten siler
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/checkout") // girilen customer id sine göre o müşterinin sepetini ORDERED hale getirir yani ckeckout olur
    public ResponseEntity<CartDto> checkout(@PathVariable Long customerId) {
        CartDto checked = cartService.checkout(customerId);
        return ResponseEntity.ok(checked);
    }
}



