package com.example.shop.shop.service;

import com.example.shop.shop.model.Cart;
import com.example.shop.shop.model.CartStatus;
import com.example.shop.shop.repository.CartRepository;
import com.example.shop.shop.service.impl.CartServiceImpl;
import com.example.shop.shop.dto.CartDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock
    private CartRepository cartRepo;
    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("Aktif sepet varsa kaydetmeden döndürür")
    void getCart_whenActiveCartExists() {
        // Arrange
        Cart existing = Cart.builder()
                .id(5L)
                .customerId(7L)
                .status(CartStatus.ACTIVE)
                .build();
        when(cartRepo.findByCustomerIdAndStatus(7L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        // Act
        CartDto dto = cartService.getActiveCart(7L);

        // Assert
        assertThat(dto.getId()).isEqualTo(5L);
        verify(cartRepo, never()).save(any());
    }

    @Test
    @DisplayName("Aktif sepet yoksa yeni oluşturup kaydeder")
    void getCart_whenNoActiveCart() {
        // Arrange
        when(cartRepo.findByCustomerIdAndStatus(8L, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepo.save(any(Cart.class)))
                .thenAnswer(inv -> {
                    Cart c = inv.getArgument(0);
                    c.setId(99L);
                    return c;
                });

        // Act
        CartDto dto = cartService.getActiveCart(8L);

        // Assert
        assertThat(dto.getId()).isEqualTo(99L);
        verify(cartRepo).save(any(Cart.class));
    }
}