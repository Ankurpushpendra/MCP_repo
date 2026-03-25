package com.ecommerce.controller;

import com.ecommerce.config.SecurityConfig;
import com.ecommerce.dto.request.CategoryRequest;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.service.CategoryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CategoryService categoryService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryResponse = CategoryResponse.builder()
                .id(1L).name("Electronics").description("Electronic items").build();
    }

    @Test
    void getAllCategories_Public_Returns200() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    void getCategoryById_Found_Returns200() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Electronics"));
    }

    @Test
    void getCategoryById_NotFound_Returns404() throws Exception {
        when(categoryService.getCategoryById(99L))
                .thenThrow(new ResourceNotFoundException("Category", "id", 99L));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_Admin_Returns201() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");

        when(categoryService.createCategory(any())).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createCategory_Customer_Returns403() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");

        mockMvc.perform(post("/api/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_Admin_Returns200() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics Updated");

        when(categoryService.updateCategory(anyLong(), any())).thenReturn(categoryResponse);

        mockMvc.perform(put("/api/categories/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_Admin_Returns200() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
