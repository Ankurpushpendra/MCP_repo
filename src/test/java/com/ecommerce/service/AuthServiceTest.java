package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encoded_password")
                .role(User.Role.CUSTOMER)
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("test@test.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsApiException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test@test.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void login_BadCredentials_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
