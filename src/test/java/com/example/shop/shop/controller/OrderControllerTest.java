package com.example.shop.shop.controller;

import com.example.shop.shop.dto.OrderDto;
import com.example.shop.shop.exception.GlobalExceptionHandler;
import com.example.shop.shop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/orders/from-cart/{customerId} → 201 Created + body")
    void createOrder_fromCart_created() throws Exception {
        long customerId = 10L;
        OrderDto dto = OrderDto.builder()
                .id(100L)
                .customerId(customerId)
                .status("NEW")
                .createdAt(LocalDateTime.of(2025, 8, 6, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 8, 6, 10, 0))
                .items(List.of())
                .build();

        when(orderService.createFromCart(customerId)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/orders/from-cart/{customerId}", customerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.customerId").value((int) customerId))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    @DisplayName("POST /api/v1/orders/from-cart/{customerId} → 400 (aktif sepet yok)")
    void createOrder_fromCart_noActiveCart() throws Exception {
        long customerId = 11L;
        when(orderService.createFromCart(customerId))
                .thenThrow(new RuntimeException("Aktif sepet bulunamadı: " + customerId));

        mockMvc.perform(post("/api/v1/orders/from-cart/{customerId}", customerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Aktif sepet bulunamadı: 11"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} → 200 OK")
    void getOrder_ok() throws Exception {
        long orderId = 20L;
        OrderDto dto = OrderDto.builder()
                .id(orderId)
                .customerId(5L)
                .status("NEW")
                .createdAt(LocalDateTime.of(2025, 8, 6, 9, 0))
                .updatedAt(LocalDateTime.of(2025, 8, 6, 9, 0))
                .items(List.of())
                .build();

        when(orderService.getById(orderId)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) orderId))
                .andExpect(jsonPath("$.customerId").value(5))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} → 400 (bulunamadı)")
    void getOrder_notFound() throws Exception {
        long orderId = 21L;
        when(orderService.getById(orderId))
                .thenThrow(new RuntimeException("Sipariş bulunamadı: " + orderId));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sipariş bulunamadı: 21"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/customer/{customerId} → 200 OK + liste")
    void getByCustomer_ok() throws Exception {
        long customerId = 30L;
        List<OrderDto> list = List.of(
                OrderDto.builder().id(1L).customerId(customerId).status("NEW")
                        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).items(List.of()).build(),
                OrderDto.builder().id(2L).customerId(customerId).status("NEW")
                        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).items(List.of()).build()
        );

        when(orderService.getByCustomer(customerId)).thenReturn(list);

        mockMvc.perform(get("/api/v1/orders/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status?status=COMPLETED → 200 OK")
    void updateStatus_ok() throws Exception {
        long orderId = 50L;
        String newStatus = "COMPLETED";
        OrderDto dto = OrderDto.builder()
                .id(orderId)
                .customerId(8L)
                .status(newStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.updateStatus(orderId, newStatus)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                        .param("status", newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus));
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status → 400 (geçersiz status)")
    void updateStatus_invalid() throws Exception {
        long orderId = 51L;
        String invalid = "BAD";
        when(orderService.updateStatus(orderId, invalid))
                .thenThrow(new RuntimeException("Geçersiz status: " + invalid));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                        .param("status", invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Geçersiz status: BAD"));
    }

    @Test
    @DisplayName("POST /api/v1/orders/from-cart/{customerId} → 400 (beklenmeyen hata)")
    void createOrder_unexpectedError() throws Exception {
        long customerId = 60L;
        doThrow(new RuntimeException("DB hatası"))
                .when(orderService).createFromCart(customerId);

        mockMvc.perform(post("/api/v1/orders/from-cart/{customerId}", customerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("DB hatası"));
    }
}