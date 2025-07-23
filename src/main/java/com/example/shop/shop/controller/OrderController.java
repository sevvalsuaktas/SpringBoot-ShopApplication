package com.example.shop.shop.controller;

import com.example.shop.shop.dto.OrderDto;
import com.example.shop.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Aktif sepetten sipariş oluşturur
     */
    @PostMapping("/from-cart/{customerId}")
    public ResponseEntity<OrderDto> createOrder(@PathVariable Long customerId) {
        OrderDto created = orderService.createFromCart(customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * ID ile sipariş getirir
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {
        OrderDto dto = orderService.getById(orderId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Müşterinin tüm siparişlerini listeler
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getByCustomer(@PathVariable Long customerId) {
        List<OrderDto> orders = orderService.getByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Sipariş durumu günceller
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        OrderDto updated = orderService.updateStatus(orderId, status);
        return ResponseEntity.ok(updated);
    }
}
