package com.example.shop.shop.controller;

import com.example.shop.shop.dto.UserDto;
import com.example.shop.shop.dto.AdminStatsDto;
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

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> listUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatsDto stats() {
        return adminService.getStats();
    }
}

