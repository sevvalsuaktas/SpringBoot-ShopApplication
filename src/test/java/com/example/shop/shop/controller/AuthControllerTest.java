package com.example.shop.shop.controller;

import com.example.shop.shop.dto.LoginRequest;
import com.example.shop.shop.dto.RegisterRequest;
import com.example.shop.shop.model.Role;
import com.example.shop.shop.model.User;
import com.example.shop.shop.repository.UserRepository;
import com.example.shop.shop.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register → 200 OK ve token döner")
    void register_success() throws Exception { // kayıt olma aşamasında success durumu
        // Arrange
        RegisterRequest req = RegisterRequest.builder()
                .username("ssa")
                .password("pw")
                .build();

        when(userRepo.findByUsername("ssa")).thenReturn(Optional.empty());
        when(encoder.encode("pw")).thenReturn("ENC");
        when(tokenProvider.generateToken(eq("ssa"), anySet())).thenReturn("jwt-123");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-123"));

        // Kaydedilen kullanıcı doğru mu kontrolü
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("ssa");
        assertThat(saved.getPassword()).isEqualTo("ENC");
        assertThat(saved.getRoles()).containsExactly(Role.ROLE_USER);

        // Token oluşturma doğru parametrelerle mi çağırıldı kontrolü
        verify(tokenProvider).generateToken(eq("ssa"), eq(Set.of(Role.ROLE_USER)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register → 400 Username taken (plain text body)")
    void register_usernameTaken() throws Exception { // register aşamasında kullanıcı adı zaten varsa
        // Arrange
        when(userRepo.findByUsername("su")).thenReturn(Optional.of(
                User.builder().username("su").build()
        ));

        RegisterRequest req = RegisterRequest.builder()
                .username("su")
                .password("secret")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username taken"));

        verify(userRepo, never()).save(any());
        verify(tokenProvider, never()).generateToken(anyString(), anySet());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login → 200 OK ve token döner")
    void login_success() throws Exception { // var olan kullanıcı giriş yapması durumunda success
        // Arrange
        LoginRequest req = LoginRequest.builder()
                .username("ssa")
                .password("pw")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("ssa");
        when(authManager.authenticate(any())).thenReturn(auth);

        User dbUser = User.builder()
                .username("ssa")
                .roles(Set.of(Role.ROLE_USER))
                .build();
        when(userRepo.findByUsername("ssa")).thenReturn(Optional.of(dbUser));
        when(tokenProvider.generateToken("ssa", Set.of(Role.ROLE_USER))).thenReturn("jwt-xyz");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-xyz"));

        verify(authManager).authenticate(any());
        verify(userRepo).findByUsername("ssa");
        verify(tokenProvider).generateToken("ssa", Set.of(Role.ROLE_USER));
    }
}
