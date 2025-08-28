package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.CartDto;
import com.example.shop.shop.dto.CartItemDto;
import com.example.shop.shop.dto.OrderDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.*;
import com.example.shop.shop.repository.*;
import com.example.shop.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    private CartDto toDto(Cart cart) {
        return CartDto.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .status(cart.getStatus().name())
                .items(cart.getItems().stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private CartItemDto toItemDto(CartItem item) {
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .build();
    }

    @Loggable
    @Override
    @Cacheable(value = "cart", key = "#customerId")
    public CartDto getActiveCart(Long customerId) {
        Cart cart = cartRepo.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseGet(() -> createEmptyCart(customerId));
        return toDto(cart);
    }

    private Cart createEmptyCart(Long customerId) {
        Cart newCart = new Cart();
        newCart.setCustomerId(customerId);
        newCart.setStatus(CartStatus.ACTIVE);
        newCart.setItems(new ArrayList<>());
        return cartRepo.save(newCart);
    }

    @Loggable
    @CacheEvict(value = "cart", key = "#customerId")
    @Override
    public CartItemDto addItem(Long customerId, CartItemDto dto) {
        CartDto cartDto = getActiveCart(customerId);
        Cart cart = cartRepo.findById(cartDto.getId())
                .orElseThrow();

        Product prod = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + dto.getProductId()));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(prod.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(prod)
                            .quantity(0)
                            .build();
                    cart.getItems().add(newItem);
                    return newItem;
                });
        item.setQuantity(item.getQuantity() + dto.getQuantity());
        
        CartItem saved = itemRepo.save(item);
        return toItemDto(saved);
    }

    @Loggable
    @CacheEvict(value = "cart", key = "#customerId")
    public void removeItem(Long customerId, Long cartItemId) {
        CartItem item = itemRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Sepet öğesi bulunamadı: " + cartItemId));
        itemRepo.delete(item);
    }

    @Loggable
    @Override
    @CacheEvict(value = "cart", key = "#customerId")
    @Transactional
    public OrderDto checkout(Long customerId) {
        Cart cart = cartRepo.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aktif sepet bulunamadı"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Sepet boş, checkout yapılamaz");
        }

        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.NEW)
                .totalAmount(total)
                .build();
        order = orderRepo.save(order);

        cart.setStatus(CartStatus.ORDERED);
        cartRepo.save(cart);

        return OrderDto.builder()
                .id(order.getId())
                .customerId(customerId)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .build();
    }
}