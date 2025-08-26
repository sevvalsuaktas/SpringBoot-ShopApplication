package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.PaymentRequestDto;
import com.example.shop.shop.dto.PaymentResponseDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.Order;
import com.example.shop.shop.model.OrderStatus;
import com.example.shop.shop.model.Payment;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.repository.PaymentRepository;
import com.example.shop.shop.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    @Loggable
    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        Order order = orderRepo.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı: " + request.getOrderId()));

        // Ödeme simülasyonu: her zaman başarılı
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        payment = paymentRepo.save(payment);

        // Sipariş durumunu güncelle
        order.setStatus(OrderStatus.COMPLETED);
        orderRepo.save(order);

        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .build();
    }

    public PaymentResponseDto paymentFallback(PaymentRequestDto request, Throwable ex) {
        log.warn("Ödeme servisi çağrısında hata: {} – orderId={} için fallback dönülüyor",
                ex.toString(), request.getOrderId());

        // Hata durumunda uygun şekilde geri dönüş yap
        return PaymentResponseDto.builder()
                .paymentId(null)
                .status("FAILED")
                .message("Ödeme servisine ulaşılamadı, lütfen tekrar deneyin.")
                .build();
    }
}