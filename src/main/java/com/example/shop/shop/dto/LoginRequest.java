package com.example.shop.shop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest { // login için gerekli olan bilgileri tutan dto
    private String username;
    private String password;
}