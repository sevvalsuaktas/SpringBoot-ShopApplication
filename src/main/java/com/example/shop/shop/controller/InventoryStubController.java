package com.example.shop.shop.controller;

import com.example.shop.shop.dto.InventoryDto;
import com.example.shop.shop.logging.Loggable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryStubController {

    @Loggable
    @GetMapping("/{productId}") // product id si girilen ürünün stok miktarını almak için endpoint
    public ResponseEntity<InventoryDto> getStock(@PathVariable Long productId) {
        // Tüm ürünler için demo amaçlı 100 adet stok girdim
        InventoryDto dto = InventoryDto.builder()
                .productId(productId)
                .available(100)
                .build();
        return ResponseEntity.ok(dto);
    }
}
