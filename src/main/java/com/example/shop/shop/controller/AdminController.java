package com.example.shop.shop.controller;

import com.example.shop.shop.dto.UserDto;
import com.example.shop.shop.dto.AdminStatsDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.service.UserService;
import com.example.shop.shop.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final AdminService adminService;

    @Loggable
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // sadece admin olan kullanıcılar erişir
    public List<UserDto> listUsers() { // tüm kullanıcıları DTO listesi olarak döner.
        return userService.findAllUsers();
    }

    @Loggable
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')") // sadece admin olan kullanıcılar erişir
    public AdminStatsDto stats() { //kullanıcı sayısı, ürün sayısı, sipariş sayısı, toplam gelir gibi verileri döner.
        return adminService.getStats();
    }
}

