package com.example.shop.shop.service;

import com.example.shop.shop.dto.UserDto;
import com.example.shop.shop.model.Role;
import com.example.shop.shop.model.User;
import com.example.shop.shop.repository.UserRepository;
import com.example.shop.shop.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("findAllUsers: User -> UserDto mapping doğru ve roller String'e çevrilir")
    void findAllUsers_mapsEntitiesToDtos() {
        // Arrange
        User u1 = User.builder()
                .id(1L)
                .username("ssa")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        User u2 = User.builder()
                .id(2L)
                .username("su")
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                .build();

        when(userRepo.findAll()).thenReturn(List.of(u1, u2));

        // Act
        List<UserDto> dtos = userService.findAllUsers();

        // Assert
        assertThat(dtos).hasSize(2);

        UserDto d1 = dtos.get(0);
        assertThat(d1.getId()).isEqualTo(1L);
        assertThat(d1.getUsername()).isEqualTo("ssa");
        assertThat(d1.getRoles()).containsExactlyInAnyOrder("ROLE_USER");

        UserDto d2 = dtos.get(1);
        assertThat(d2.getId()).isEqualTo(2L);
        assertThat(d2.getUsername()).isEqualTo("su");
        assertThat(d2.getRoles()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");

        verify(userRepo, times(1)).findAll();
        verifyNoMoreInteractions(userRepo);
    }

    @Test
    @DisplayName("findAllUsers: repo boş dönerse boş liste döner")
    void findAllUsers_empty() {
        when(userRepo.findAll()).thenReturn(List.of());

        List<UserDto> dtos = userService.findAllUsers();

        assertThat(dtos).isEmpty();
        verify(userRepo).findAll();
        verifyNoMoreInteractions(userRepo);
    }
}

