package com.example.shop.shop.config;

import com.example.shop.shop.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
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
                // State‑less bir REST API için oturumu yönetmeyiz
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CSRF’yi sadece form‑login için kullanırız, API’da kapatıyoruz
                .csrf(csrf -> csrf.disable())

                // Basic Auth ve Form‑Login’i kapatıyoruz (JWT kullanacağız)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())

                // İzinler
                .authorizeHttpRequests(auth -> auth
                        // Kayıt/giriş açık
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Yalnızca ADMIN erişebilsin
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Geri kalan tüm isteklere authentication zorunlu
                        .anyRequest().authenticated()
                )

                // JWT filtresini UsernamePasswordAuthenticationFilter’dan önce çalıştır
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}



