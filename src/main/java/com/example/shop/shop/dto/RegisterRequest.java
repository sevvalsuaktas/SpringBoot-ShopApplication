package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest { // kayıt olurken istenen bilgiler
    private String username;
    private String password;
}

