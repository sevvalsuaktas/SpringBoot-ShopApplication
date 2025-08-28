package com.example.shop.shop.controller;

import com.example.shop.shop.dto.CartDto;
import com.example.shop.shop.exception.GlobalExceptionHandler;
import com.example.shop.shop.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
    private MockMvc mockMvc;
    @Mock
    private CartService cartService;
    @InjectMocks
    private CartController cartController;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cartController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/cart/{id} → 200 ve doğru JSON")
    void getCart_success() throws Exception {
        // Arrange(hazırlık)
        CartDto sample = CartDto.builder()
                .id(3L)
                .customerId(12L)
                .status("ACTIVE")
                .build();
        when(cartService.getActiveCart(12L)).thenReturn(sample);

        // Act & Assert(eylem ve doğrulama)
        mockMvc.perform(get("/api/v1/cart/12")) // sanki HTTP GET çağrısı yapıyormuşuz gibi controller ı çağırıyoruz
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.customerId").value(12))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Eksik parametreyle çağrı → 404 Not Found")
    void getCart_missingId_badRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cart/"))
                .andExpect(status().isNotFound());
    }
}