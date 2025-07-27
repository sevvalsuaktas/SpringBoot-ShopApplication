package com.example.shop.shop.config;

import com.example.shop.shop.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless REST API: session yönetimini kapat
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CSRF, Basic Auth, Form‑Login kapalı
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())

                // Yetkilendirme kuralları
                .authorizeHttpRequests(auth -> auth
                        // Auth işlemleri herkese açık
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Admin-only endpoint’leriniz (kullanıcı listesi, istatistik vb.)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Kategori yönetimi: sadece ADMIN
                        .requestMatchers(HttpMethod.POST,   "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")

                        // Ürün yönetimi: sadece ADMIN
                        .requestMatchers(HttpMethod.POST,   "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")

                        // Geri kalan (GET + diğer endpoint’ler): oturum açmış kullanıcı
                        .anyRequest().authenticated()
                )

                // JWT filtresini Spring Security zincirine ekle
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Şifreleri BCrypt ile hash’lemek için
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager bean’ini expose et
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
