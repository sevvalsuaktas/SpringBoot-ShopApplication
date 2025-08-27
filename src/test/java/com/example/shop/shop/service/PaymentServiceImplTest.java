package com.example.shop.shop.service;

import com.example.shop.shop.dto.PaymentRequestDto;
import com.example.shop.shop.dto.PaymentResponseDto;
import com.example.shop.shop.model.Order;
import com.example.shop.shop.model.OrderStatus;
import com.example.shop.shop.model.Payment;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.repository.PaymentRepository;
import com.example.shop.shop.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private OrderRepository orderRepo;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("processPayment: sipariş varsa ödeme SUCCESS kaydedilir ve sipariş COMPLETED olur")
    void processPayment_success() {
        // Arrange
        long orderId = 123L;
        PaymentRequestDto req = PaymentRequestDto.builder()
                .orderId(orderId)
                .amount(49.99)
                .method("CARD")
                .build();

        Order existing = Order.builder()
                .id(orderId)
                .status(OrderStatus.NEW)
                .build();

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(existing));

        // paymentRepo.save döndürülen entity'e id setleyelim ki response'ta dönsün
        when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            // id database de oluşmuş gibi simüle edildi
            return Payment.builder()
                    .id(999L)
                    .orderId(p.getOrderId())
                    .amount(p.getAmount())
                    .method(p.getMethod())
                    .status(p.getStatus())
                    .build();
        });

        // Act
        PaymentResponseDto resp = paymentService.processPayment(req);

        // Assert — DTO
        assertThat(resp.getPaymentId()).isEqualTo(999L);
        assertThat(resp.getStatus()).isEqualTo("SUCCESS");
        assertThat(resp.getMessage()).isNull();

        // Assert — paymentRepo'ya kaydedilen
        ArgumentCaptor<Payment> payCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepo, times(1)).save(payCaptor.capture());
        Payment savedPayment = payCaptor.getValue();
        assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
        assertThat(savedPayment.getAmount()).isEqualTo(49.99);
        assertThat(savedPayment.getMethod()).isEqualTo("CARD");
        assertThat(savedPayment.getStatus()).isEqualTo("SUCCESS");

        // Assert — orderRepo.save çağrısında statü COMPLETED olmuş mu kontrolü
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo, times(1)).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        verify(orderRepo).findById(orderId);
        verifyNoMoreInteractions(orderRepo, paymentRepo);
    }

    @Test
    @DisplayName("processPayment: sipariş yoksa 'Sipariş bulunamadı' hatası fırlatır")
    void processPayment_orderNotFound_throws() {
        // Arrange
        long orderId = 456L;
        PaymentRequestDto req = PaymentRequestDto.builder()
                .orderId(orderId)
                .amount(10.0)
                .method("CARD")
                .build();

        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sipariş bulunamadı: 456");

        verify(orderRepo).findById(orderId);
        verifyNoInteractions(paymentRepo);
        verify(orderRepo, never()).save(any());
    }

    @Test
    @DisplayName("paymentFallback: FAILED döndürür ve mesaj içerir")
    void paymentFallback_returnsFailed() {
        // Arrange
        PaymentRequestDto req = PaymentRequestDto.builder()
                .orderId(789L)
                .amount(1.0)
                .method("CARD")
                .build();

        RuntimeException ex = new RuntimeException("downstream");

        // Act
        PaymentResponseDto resp = paymentService.paymentFallback(req, ex);

        // Assert
        assertThat(resp.getPaymentId()).isNull();
        assertThat(resp.getStatus()).isEqualTo("FAILED");
        assertThat(resp.getMessage()).contains("Ödeme servisine ulaşılamadı");
    }
}