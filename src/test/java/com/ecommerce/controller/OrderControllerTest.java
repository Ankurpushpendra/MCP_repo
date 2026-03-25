package com.ecommerce.controller;

import com.ecommerce.config.SecurityConfig;
import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.service.OrderService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderResponse = OrderResponse.builder()
                .id(1L).userId(1L).userEmail("user@test.com")
                .status("PENDING").totalAmount(new BigDecimal("999.99"))
                .shippingAddress("123 Main St")
                .orderItems(List.of())
                .build();
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getOrders_Authenticated_Returns200() throws Exception {
        when(orderService.getOrders(anyString(), anyBoolean())).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getOrderById_Found_Returns200() throws Exception {
        when(orderService.getOrderById(anyString(), anyLong(), anyBoolean()))
                .thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(999.99));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getOrderById_NotFound_Returns404() throws Exception {
        when(orderService.getOrderById(anyString(), anyLong(), anyBoolean()))
                .thenThrow(new ResourceNotFoundException("Order", "id", 99L));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void createOrder_Authenticated_Returns201() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setShippingAddress("123 Main St");

        when(orderService.createOrder(anyString(), any())).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shippingAddress").value("123 Main St"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void updateOrderStatus_Admin_Returns200() throws Exception {
        OrderResponse updated = OrderResponse.builder()
                .id(1L).status("PROCESSING").totalAmount(new BigDecimal("999.99"))
                .orderItems(List.of()).build();

        when(orderService.updateOrderStatus(1L, "PROCESSING")).thenReturn(updated);

        mockMvc.perform(put("/api/orders/1/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PROCESSING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "CUSTOMER")
    void updateOrderStatus_Customer_Returns403() throws Exception {
        mockMvc.perform(put("/api/orders/1/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PROCESSING"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void updateOrderStatus_MissingStatus_Returns400() throws Exception {
        mockMvc.perform(put("/api/orders/1/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());
    }
}
