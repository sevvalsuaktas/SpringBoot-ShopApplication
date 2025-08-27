package com.example.shop.shop.service;

import com.example.shop.shop.dto.OrderDto;
import com.example.shop.shop.model.*;
import com.example.shop.shop.repository.CartItemRepository;
import com.example.shop.shop.repository.CartRepository;
import com.example.shop.shop.repository.OrderItemRepository;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private CartRepository cartRepo;

    @Mock
    private CartItemRepository cartItemRepo; // Serviste field var; testte de mock’layalım

    @Mock
    private OrderItemRepository orderItemRepo;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("createFromCart: aktif sepet yoksa exception fırlatır")
    void createFromCart_noActiveCart_throwsException() {
        // Arrange
        when(cartRepo.findByCustomerIdAndStatus(1L, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.checkout(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aktif sepet bulunamadı: 1");
    }

    @Test
    @DisplayName("createFromCart: boş sepet için exception fırlatır")
    void createFromCart_emptyCart_throwsException() {
        // Arrange
        Cart emptyCart = Cart.builder()
                .customerId(2L)
                .status(CartStatus.ACTIVE)
                .items(List.of())
                .build();

        when(cartRepo.findByCustomerIdAndStatus(2L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(emptyCart));

        // Act & Assert
        assertThatThrownBy(() -> orderService.checkout(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sepet boş");
    }

    @Test
    @DisplayName("getById: var olan siparişi döndürür")
    void getById_existingOrder_returnsDto() {
        // Arrange
        long orderId = 200L;

        Order order = Order.builder()
                .id(orderId)
                .customerId(5L)
                .status(OrderStatus.NEW)
                .items(List.of())
                .build();

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderDto dto = orderService.getById(orderId);

        // Assert
        assertThat(dto.getId()).isEqualTo(orderId);
        assertThat(dto.getStatus()).isEqualTo(OrderStatus.NEW.name());
    }

    @Test
    @DisplayName("getById: olmayan sipariş için exception fırlatır")
    void getById_missingOrder_throwsException() {
        when(orderRepo.findById(300L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getById(300L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sipariş bulunamadı: 300");
    }

    @Test
    @DisplayName("getByCustomer: müşteri siparişlerini döndürür")
    void getByCustomer_returnsDtos() {
        // Arrange
        long customerId = 6L;
        Order o1 = Order.builder().id(1L).customerId(customerId)
                .status(OrderStatus.NEW)
                .items(List.of())
                .build();
        Order o2 = Order.builder().id(2L).customerId(customerId)
                .status(OrderStatus.NEW)
                .items(List.of())
                .build();

        when(orderRepo.findByCustomerId(customerId)).thenReturn(List.of(o1, o2));

        // Act
        List<OrderDto> dtos = orderService.getByCustomer(customerId);

        // Assert
        assertThat(dtos).hasSize(2)
                .extracting(OrderDto::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("updateStatus: var olan siparişin statüsünü günceller")
    void updateStatus_validStatus_updatesDto() {
        // Arrange
        long orderId = 400L;
        Order existing = Order.builder()
                .id(orderId)
                .customerId(7L)
                .status(OrderStatus.NEW)
                .items(List.of())
                .build();

        Order updated = Order.builder()
                .id(orderId)
                .customerId(7L)
                .status(OrderStatus.COMPLETED)
                .items(List.of())
                .build();

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(existing));
        when(orderRepo.save(any(Order.class))).thenReturn(updated);

        // Act
        OrderDto dto = orderService.updateStatus(orderId, "COMPLETED");

        // Assert
        assertThat(dto.getStatus()).isEqualTo(OrderStatus.COMPLETED.name());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("updateStatus: olmayan sipariş için exception fırlatır")
    void updateStatus_missingOrder_throwsException() {
        when(orderRepo.findById(500L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.updateStatus(500L, "ANY"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sipariş bulunamadı: 500");
    }

    @Test
    @DisplayName("updateStatus: geçersiz status için exception fırlatır")
    void updateStatus_invalidStatus_throwsException() {
        // Arrange
        Order order = Order.builder()
                .id(600L)
                .customerId(8L)
                .status(OrderStatus.NEW)
                .items(List.of())
                .build();

        when(orderRepo.findById(600L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateStatus(600L, "INVALID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Geçersiz status: INVALID");
    }
}



