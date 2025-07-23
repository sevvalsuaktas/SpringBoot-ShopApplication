package com.example.shop.shop;

import com.example.shop.shop.model.Role;
import com.example.shop.shop.model.User;
import com.example.shop.shop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
@EnableFeignClients
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepo,
                                       PasswordEncoder encoder) {
        return args -> {
            String adminUsername = "admin";
            // Eğer admin kullanıcı yoksa oluştur
            if (userRepo.findByUsername(adminUsername).isEmpty()) {
                User admin = User.builder()
                        .username(adminUsername)
                        .password(encoder.encode("admin123"))
                        .roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
                        .build();
                userRepo.save(admin);
                System.out.println("▶ Default admin created: admin / admin123");
            }
        };
    }
}


