package com.ecommerce.security;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_Found_ReturnsUserDetails() {
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .role(User.Role.CUSTOMER)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@test.com");

        assertNotNull(result);
        assertEquals("user@test.com", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nobody@test.com"));
    }
}
