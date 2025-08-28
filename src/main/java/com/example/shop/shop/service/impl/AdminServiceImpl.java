package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.AdminStatsDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.repository.UserRepository;
import com.example.shop.shop.repository.ProductRepository;
import com.example.shop.shop.repository.OrderRepository;
import com.example.shop.shop.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    @Loggable
    @Override
    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        long userCount = userRepo.count();
        long productCount = productRepo.count();
        long orderCount = orderRepo.count();

        return AdminStatsDto.builder()
                .userCount(userCount)
                .productCount(productCount)
                .orderCount(orderCount)
                .build();
    }
}