package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.OrderDto;
import com.example.shop.shop.dto.OrderItemDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.Cart;
import com.example.shop.shop.model.CartStatus;
import com.example.shop.shop.model.Order;
import com.example.shop.shop.model.OrderItem;
import com.example.shop.shop.model.OrderStatus;
import com.example.shop.shop.repository.CartRepository;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;

    /*@Loggable
    private OrderItemDto toItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .build();
    }*/

    @Loggable
    private OrderDto toDto(Order o) {
        return OrderDto.builder()
                .id(o.getId())
                .customerId(o.getCustomerId())
                .status(o.getStatus().name())
                .totalAmount(o.getTotalAmount())
                .items(
                        o.getItems() == null ? List.of() :
                                o.getItems().stream()
                                        .map(oi -> OrderItemDto.builder()
                                                .id(oi.getId())
                                                .productId(oi.getProduct().getId())
                                                .quantity(oi.getQuantity())
                                                .build())
                                        .toList()
                )
                .build();
    }

    @Loggable
    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public OrderDto getById(Long orderId) {
        return orderRepo.findById(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: " + orderId));
    }

    @Loggable
    @Override
    @Cacheable(value = "customerOrders", key = "#customerId")
    public List<OrderDto> getByCustomer(Long customerId) {
        return orderRepo.findByCustomerId(customerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Loggable
    @Override
    @CacheEvict(value = {"orders", "customerOrders"}, allEntries = true)
    public OrderDto updateStatus(Long orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: " + orderId));
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Geçersiz status: " + status);
        }
        order.setStatus(newStatus);
        order = orderRepo.save(order);
        return toDto(order);
    }

    @Loggable
    @Override
    @CacheEvict(value = {"orders", "customerOrders"}, allEntries = true)
    @Transactional
    public OrderDto checkout(Long customerId) {
        // 1) Aktif sepeti bul
        Cart cart = cartRepo.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aktif sepet bulunamadı: " + customerId));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Sepet boş");
        }

        // 2) Toplam tutarı BigDecimal ile hesapla
        BigDecimal total = cart.getItems().stream()
                .map(ci -> ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3) Order oluştur
        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.NEW)
                .totalAmount(total)
                .build();

        // 4) CartItem -> OrderItem
        Order finalOrder = order;
        List<OrderItem> items = cart.getItems().stream()
                .map(ci -> OrderItem.builder()
                        .order(finalOrder)
                        .product(ci.getProduct())
                        .quantity(ci.getQuantity())
                        .build())
                .collect(Collectors.toList());
        order.setItems(items);

        // 5) Kaydet (Order.items ilişkisinde cascade=ALL varsa bu tek save yeter)
        order = orderRepo.save(order);

        // 6) Sepeti kapat
        cart.setStatus(CartStatus.ORDERED);
        cartRepo.save(cart);

        // 7) DTO döndür
        return toDto(order);
    }
}