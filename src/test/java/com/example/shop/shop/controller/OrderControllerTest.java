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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;

import static org.mockito.Mockito.verify;
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
    @DisplayName("GET /api/v1/orders/{orderId} → 200 OK")
    void getOrder_ok() throws Exception {
        long orderId = 20L;
        OrderDto dto = OrderDto.builder()
                .id(orderId)
                .customerId(5L)
                .status("NEW")
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
                OrderDto.builder().id(1L).customerId(customerId).build(),
                OrderDto.builder().id(2L).customerId(customerId).build()
        );

        when(orderService.getByCustomer(customerId)).thenReturn(list);

        mockMvc.perform(get("/api/v1/orders/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(orderService).getByCustomer(customerId);
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
}