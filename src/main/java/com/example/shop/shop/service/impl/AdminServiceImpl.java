package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.AdminStatsDto;
import com.example.shop.shop.repository.UserRepository;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepo; // kullanıcı tablosu üzerine CRUD işlemleri ve sayma işlemleri yapıcak UserRepository
    private final ProductRepository productRepo; // ürün tablosu için
    private final OrderRepository orderRepo; // sipariş tablosu için

    @Override
    public AdminStatsDto getStats() { // istatistiklere erişebilmek için bir metot (admin-only)
        long userCount = userRepo.count();
        long productCount = productRepo.count();
        long orderCount = orderRepo.count();
        double totalRevenue = orderRepo.findAll().stream()
                .flatMap(o -> o.getItems().stream())
                .mapToDouble(i -> i.getPriceAtPurchase() * i.getQuantity())
                .sum();

        return AdminStatsDto.builder() // bu verileri adminstatsdto içinde paketleip döner
                .userCount(userCount)
                .productCount(productCount)
                .orderCount(orderCount)
                .totalRevenue(totalRevenue)
                .build();
    }
}