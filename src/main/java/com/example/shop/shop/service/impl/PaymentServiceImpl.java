package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.PaymentRequestDto;
import com.example.shop.shop.dto.PaymentResponseDto;
import com.example.shop.shop.model.Order;
import com.example.shop.shop.model.OrderStatus;
import com.example.shop.shop.model.Payment;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.repository.PaymentRepository;
import com.example.shop.shop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    @Override
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
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);

        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .build();
    }
}
