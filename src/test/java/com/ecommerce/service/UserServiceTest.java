package com.ecommerce.service;

import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .role(User.Role.CUSTOMER)
                .build();
    }

    @Test
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("user@test.com", result.get(0).getEmail());
    }

    @Test
    void getUserById_Found_ReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("user@test.com", result.getEmail());
        assertEquals("CUSTOMER", result.getRole());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void getUserByEmail_Found_ReturnsUser() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByEmail("user@test.com");

        assertEquals("user@test.com", result.getEmail());
    }

    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("nobody@test.com"));
    }
}
