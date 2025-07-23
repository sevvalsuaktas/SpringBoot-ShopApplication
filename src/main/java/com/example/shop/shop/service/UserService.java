package com.example.shop.shop.service;

import com.example.shop.shop.dto.UserDto;
import java.util.List;

public interface UserService {
    List<UserDto> findAllUsers();
}

