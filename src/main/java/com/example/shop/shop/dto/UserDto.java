package com.example.shop.shop.dto;

import lombok.*;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto { // Controller’dan client’a giden kullanıcı verisi
    private Long id;
    private String username;
    private Set<String> roles;
}
