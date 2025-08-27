package com.example.shop.shop.service;

import com.example.shop.shop.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto getById(Long orderId);
    List<OrderDto> getByCustomer(Long customerId);
    OrderDto updateStatus(Long orderId, String status);
    OrderDto checkout(Long customerId);
}

