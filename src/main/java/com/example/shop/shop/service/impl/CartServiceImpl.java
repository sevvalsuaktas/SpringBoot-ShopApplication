package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.CartDto;
import com.example.shop.shop.dto.CartItemDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.Cart;
import com.example.shop.shop.model.CartItem;
import com.example.shop.shop.model.CartStatus;
import com.example.shop.shop.model.Product;
import com.example.shop.shop.repository.CartRepository;
import com.example.shop.shop.repository.CartItemRepository;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;

    @Loggable
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

    @Loggable
    private CartItemDto toItemDto(CartItem item) {
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .build();
    }

    @Loggable
    @Override
    public CartDto getActiveCart(Long customerId) {
        Cart cart = cartRepo.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE) // müşteriye ait active sepet var mı kontrol eder
                .orElseGet(() -> cartRepo.save( // yoksa oluştur
                        Cart.builder()
                                .customerId(customerId)
                                .status(CartStatus.ACTIVE)
                                .build()
                ));
        return toDto(cart);
    }

    @Loggable
    @Override
    public CartItemDto addItem(Long customerId, CartItemDto dto) {
        Cart cart = cartRepo.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE) // müşterinin aktif sepeti yoksa hata fırlatır
                .orElseThrow(() -> new RuntimeException("Aktif sepet bulunamadı"));
        Product prod = productRepo.findById(dto.getProductId()) // verilen product id ye uygun product yoksa hata fırlatır
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + dto.getProductId()));

        // Eğer aynı üründen zaten eklenmişse, miktarı güncelle
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
    @Override
    public void removeItem(Long cartItemId) {
        CartItem item = itemRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Sepet öğesi bulunamadı: " + cartItemId));
        itemRepo.delete(item);
    }

    @Loggable
    @Override
    public CartDto checkout(Long customerId) {
        Cart cart = cartRepo.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aktif sepet bulunamadı"));
        cart.setStatus(CartStatus.ORDERED);
        Cart saved = cartRepo.save(cart);
        return toDto(saved);
    }
}

