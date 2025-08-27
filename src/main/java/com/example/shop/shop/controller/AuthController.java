package com.example.shop.shop.controller;

import com.example.shop.shop.dto.RegisterRequest;
import com.example.shop.shop.dto.LoginRequest;
import com.example.shop.shop.dto.AuthResponse;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.model.Role;
import com.example.shop.shop.model.User;
import com.example.shop.shop.repository.UserRepository;
import com.example.shop.shop.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider tokenProvider,
                          UserRepository userRepo,
                          PasswordEncoder encoder) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Loggable
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username taken");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .build();
        userRepo.save(user);

        String token = tokenProvider.generateToken(
                user.getUsername(),
                user.getRoles()
        );

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Loggable
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow();

        String token = tokenProvider.generateToken(
                user.getUsername(),
                user.getRoles()
        );
        return ResponseEntity.ok(new AuthResponse(token));
    }
}