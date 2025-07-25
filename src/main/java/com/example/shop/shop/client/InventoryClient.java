package com.example.shop.shop.client;

import com.example.shop.shop.dto.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory", url = "${inventory.service.url}")
public interface InventoryClient {
    @GetMapping("/api/v1/inventory/{productId}")
    InventoryDto getStock(@PathVariable("productId") Long productId); // her ürün için mevcut stok adedini almak için
}
