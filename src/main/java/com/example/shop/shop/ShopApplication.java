package com.example.shop.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.shop.shop.model.Role;
import com.example.shop.shop.model.User;
import com.example.shop.shop.repository.UserRepository;

import java.util.Set;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
public class ShopApplication implements ApplicationRunner {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(">>> inventory.service.url = " + env.getProperty("inventory.service.url"));
        System.out.println(">>> Active CacheManager class: " + cacheManager.getClass().getName());
    }

    @Bean
    @ConditionalOnBean(UserRepository.class)
    public CommandLineRunner seedAdmin(UserRepository userRepo, PasswordEncoder encoder) {
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