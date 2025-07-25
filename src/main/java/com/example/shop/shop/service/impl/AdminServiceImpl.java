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
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    @Override
    public AdminStatsDto getStats() {
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