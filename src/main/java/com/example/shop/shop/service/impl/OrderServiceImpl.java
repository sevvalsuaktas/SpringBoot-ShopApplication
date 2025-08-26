package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.OrderDto;
import com.example.shop.shop.dto.OrderItemDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.Cart;
import com.example.shop.shop.model.CartStatus;
import com.example.shop.shop.model.Order;
import com.example.shop.shop.model.OrderItem;
import com.example.shop.shop.model.OrderStatus;
import com.example.shop.shop.repository.CartItemRepository;
import com.example.shop.shop.repository.CartRepository;
import com.example.shop.shop.repository.OrderItemRepository;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final OrderItemRepository orderItemRepo;

    @Loggable
    private OrderItemDto toItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }

    @Loggable
    private OrderDto toDto(Order o) {
        return OrderDto.builder()
                .id(o.getId())
                .customerId(o.getCustomerId())
                .status(o.getStatus().name())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .items(
                        o.getItems() == null ? List.of() :
                                o.getItems().stream()
                                        .map(oi -> OrderItemDto.builder()
                                                .id(oi.getId())
                                                .productId(oi.getProduct().getId())
                                                .quantity(oi.getQuantity())
                                                .priceAtPurchase(oi.getPriceAtPurchase())
                                                .build())
                                        .toList()
                )
                .build();
    }

    @Loggable
    @Override
    @CacheEvict(value = {"orders", "customerOrders"}, allEntries = true) // yeni bir sipariş oluşturulduğunda cache i temizle
    public OrderDto createFromCart(Long customerId) {
        Cart cart = cartRepo.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aktif sepet bulunamadı: " + customerId));
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Sepet boş");
        }

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.NEW)
                .build();
        order = orderRepo.save(order);

        Order finalOrder = order;
        List<OrderItem> items = cart.getItems().stream()
                .map(ci -> OrderItem.builder()
                        .order(finalOrder)
                        .product(ci.getProduct())
                        .quantity(ci.getQuantity())
                        .priceAtPurchase(ci.getProduct().getPrice())
                        .build()
                )
                .collect(Collectors.toList());
        orderItemRepo.saveAll(items);

        cart.setStatus(CartStatus.ORDERED);
        cartRepo.save(cart);

        order.setItems(items);
        return toDto(order);
    }

    @Loggable
    @Override
    @Cacheable(value = "orders", key = "#orderId") // tek bir siparişi cache ler
    public OrderDto getById(Long orderId) {
        return orderRepo.findById(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: " + orderId));
    }

    @Loggable
    @Override
    @Cacheable(value = "customerOrders", key = "#customerId") // müşteriye ait tüm siparişleri cache ler
    public List<OrderDto> getByCustomer(Long customerId) {
        return orderRepo.findByCustomerId(customerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Loggable
    @Override
    @CacheEvict(value = {"orders", "customerOrders"}, allEntries = true) // sipariş durumu güncellendiğinde cache leri temizle
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
}

// getById metodu id ye göre tek bir siparişi orders cache ine;
// getByCustomer metodu musteriIdye göre tüm siparişi customerOrders cache ine kaydeder
