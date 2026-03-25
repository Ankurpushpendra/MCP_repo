package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private ProductService productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Electronics").build();
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("A laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .category(category)
                .build();
    }

    @Test
    void getAllProducts_ReturnsPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = productService.getAllProducts(0, 10, null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    void getAllProducts_WithCategoryFilter_ReturnsFilteredPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = productService.getAllProducts(0, 10, 1L, null);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllProducts_WithSearchFilter_ReturnsFilteredPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = productService.getAllProducts(0, 10, null, "laptop");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductById_Found_ReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(0, new BigDecimal("999.99").compareTo(result.getPrice()));
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void createProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setName("Phone");
        request.setDescription("A phone");
        request.setPrice(new BigDecimal("499.99"));
        request.setStockQuantity(20);
        request.setCategoryId(1L);

        Product saved = Product.builder().id(2L).name("Phone").price(new BigDecimal("499.99"))
                .stockQuantity(20).category(category).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse result = productService.createProduct(request);

        assertEquals("Phone", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_CategoryNotFound_ThrowsException() {
        ProductRequest request = new ProductRequest();
        request.setName("Phone");
        request.setPrice(new BigDecimal("499.99"));
        request.setStockQuantity(10);
        request.setCategoryId(99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop Pro");
        request.setDescription("Updated");
        request.setPrice(new BigDecimal("1299.99"));
        request.setStockQuantity(5);
        request.setCategoryId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenReturn(product);

        ProductResponse result = productService.updateProduct(1L, request);

        assertNotNull(result);
        verify(productRepository).save(any());
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(99L, new ProductRequest()));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
        verify(productRepository, never()).deleteById(any());
    }
}
