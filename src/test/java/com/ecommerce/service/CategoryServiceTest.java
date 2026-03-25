package com.ecommerce.service;

import com.ecommerce.dto.request.CategoryRequest;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.ApiException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private CategoryService categoryService;

    private Category electronics;

    @BeforeEach
    void setUp() {
        electronics = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic items")
                .build();
    }

    @Test
    void getAllCategories_ReturnsList() {
        when(categoryRepository.findAll()).thenReturn(List.of(electronics));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void getCategoryById_Found_ReturnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Electronics", result.getName());
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void createCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");
        request.setDescription("All books");

        Category saved = Category.builder().id(2L).name("Books").description("All books").build();

        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.createCategory(request);

        assertEquals("Books", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsApiException() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThrows(ApiException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics Updated");
        request.setDescription("Updated desc");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
        when(categoryRepository.existsByName("Electronics Updated")).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(electronics);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertNotNull(result);
        verify(categoryRepository).save(any());
    }

    @Test
    void updateCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(99L, new CategoryRequest()));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
        verify(categoryRepository, never()).deleteById(any());
    }
}
