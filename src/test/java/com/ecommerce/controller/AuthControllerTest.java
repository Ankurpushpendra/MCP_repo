package com.ecommerce.controller;

import com.ecommerce.config.SecurityConfig;
import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_Success_Returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.of(1L, "new@test.com", "CUSTOMER");
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new@test.com"));
    }

    @Test
    void register_BlankEmail_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShortPassword_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("abc");

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success_Returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.of(1L, "user@test.com", "CUSTOMER");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    void login_BadCredentials_Returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
