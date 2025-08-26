package com.example.shop.shop.controller;

import com.example.shop.shop.dto.PaymentRequestDto;
import com.example.shop.shop.dto.PaymentResponseDto;
import com.example.shop.shop.exception.GlobalExceptionHandler;
import com.example.shop.shop.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/payments → 200 OK ve PaymentResponseDto JSON döner")
    void pay_success() throws Exception {
        // Arrange — örnek istek/yanıt
        PaymentRequestDto req = PaymentRequestDto.builder()
                .orderId(123L)
                .amount(49.99)
                .method("CARD")
                .build();

        PaymentResponseDto resp = PaymentResponseDto.builder()
                .status("APPROVED")
                .message("Payment successful")
                .build();

        when(paymentService.processPayment(any(PaymentRequestDto.class))).thenReturn(resp);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.message").value("Payment successful"));

        ArgumentCaptor<PaymentRequestDto> captor = ArgumentCaptor.forClass(PaymentRequestDto.class);
        verify(paymentService, times(1)).processPayment(captor.capture());
        PaymentRequestDto passed = captor.getValue();
        assertThat(passed.getOrderId()).isEqualTo(123L);
        assertThat(passed.getAmount()).isEqualTo(49.99);
        assertThat(passed.getMethod()).isEqualTo("CARD");
    }

    @Test
    @DisplayName("POST /api/v1/payments → servis hata fırlatırsa 400 ve mesaj döner")
    void pay_failure_returnsBadRequest() throws Exception {
        PaymentRequestDto req = PaymentRequestDto.builder()
                .orderId(999L)
                .amount(10.0)
                .method("CARD")
                .build();

        when(paymentService.processPayment(any(PaymentRequestDto.class)))
                .thenThrow(new RuntimeException("Payment failed"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment failed"));
    }
}

