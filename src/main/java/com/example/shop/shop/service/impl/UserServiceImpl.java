package com.example.shop.shop.service.impl;

import com.example.shop.shop.dto.UserDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.User;
import com.example.shop.shop.repository.UserRepository;
import com.example.shop.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;

    @Loggable
    @Override
    public List<UserDto> findAllUsers() {
        return userRepo.findAll().stream()// repo dan tüm User entity lerini alır
                .map(u -> UserDto.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .roles(u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                        .build()
                )
                .collect(Collectors.toList());
    }
}

