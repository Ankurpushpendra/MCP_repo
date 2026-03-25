package com.ecommerce.controller;

import com.ecommerce.config.SecurityConfig;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProductService productService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id(1L).name("Laptop").description("A laptop")
                .price(new BigDecimal("999.99")).stockQuantity(10)
                .categoryId(1L).categoryName("Electronics")
                .build();
    }

    @Test
    void getAllProducts_Public_Returns200() throws Exception {
        when(productService.getAllProducts(anyInt(), anyInt(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(productResponse)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Laptop"));
    }

    @Test
    void getProductById_Found_Returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Laptop"))
                .andExpect(jsonPath("$.data.price").value(999.99));
    }

    @Test
    void getProductById_NotFound_Returns404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Product", "id", 99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_Admin_Returns201() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Phone");
        request.setPrice(new BigDecimal("499.99"));
        request.setStockQuantity(20);
        request.setCategoryId(1L);

        when(productService.createProduct(any())).thenReturn(productResponse);

        mockMvc.perform(post("/api/products").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createProduct_Customer_Returns403() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Phone");
        request.setPrice(new BigDecimal("499.99"));
        request.setStockQuantity(20);
        request.setCategoryId(1L);

        mockMvc.perform(post("/api/products").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_Admin_Returns200() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop Pro");
        request.setPrice(new BigDecimal("1299.99"));
        request.setStockQuantity(5);
        request.setCategoryId(1L);

        when(productService.updateProduct(anyLong(), any())).thenReturn(productResponse);

        mockMvc.perform(put("/api/products/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_Admin_Returns200() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
