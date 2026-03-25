package com.ecommerce.controller;

import com.ecommerce.config.SecurityConfig;
import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CartService cartService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        CartResponse.CartItemResponse item = CartResponse.CartItemResponse.builder()
                .id(1L).productId(1L).productName("Laptop")
                .unitPrice(new BigDecimal("999.99")).quantity(2)
                .subtotal(new BigDecimal("1999.98"))
                .build();

        cartResponse = CartResponse.builder()
                .items(List.of(item))
                .totalAmount(new BigDecimal("1999.98"))
                .totalItems(2)
                .build();
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getCart_Authenticated_Returns200() throws Exception {
        when(cartService.getCart("user@test.com")).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(2));
    }

    @Test
    void getCart_Unauthenticated_Returns3xxOr401() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(result ->
                        assertTrue(result.getResponse().getStatus() == 302 ||
                                result.getResponse().getStatus() == 401));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void addItem_Authenticated_Returns200() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartService.addItem(anyString(), any())).thenReturn(cartResponse);

        mockMvc.perform(post("/api/cart").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void updateItem_Authenticated_Returns200() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        when(cartService.updateItem(anyString(), anyLong(), any())).thenReturn(cartResponse);

        mockMvc.perform(put("/api/cart/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void removeItem_Authenticated_Returns200() throws Exception {
        doNothing().when(cartService).removeItem("user@test.com", 1L);

        mockMvc.perform(delete("/api/cart/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void clearCart_Authenticated_Returns200() throws Exception {
        doNothing().when(cartService).clearCart("user@test.com");

        mockMvc.perform(delete("/api/cart").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("Expected redirect or 401");
    }
}
