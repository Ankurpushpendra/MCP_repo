package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ApiException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CartService cartService;

    private User user;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").role(User.Role.CUSTOMER).build();
        product = Product.builder()
                .id(1L).name("Laptop").price(new BigDecimal("999.99")).stockQuantity(10)
                .category(Category.builder().id(1L).name("Electronics").build())
                .build();
        cartItem = CartItem.builder().id(1L).user(user).product(product).quantity(2).build();
    }

    @Test
    void getCart_ReturnsCartResponse() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse result = cartService.getCart("user@test.com");

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getTotalItems());
        assertEquals(0, new BigDecimal("1999.98").compareTo(result.getTotalAmount()));
    }

    @Test
    void getCart_EmptyCart_ReturnsEmptyResponse() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        CartResponse result = cartService.getCart("user@test.com");

        assertTrue(result.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
    }

    @Test
    void addItem_NewItem_Success() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any())).thenReturn(cartItem);
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse result = cartService.addItem("user@test.com", request);

        assertNotNull(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_ExistingItem_UpdatesQuantity() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any())).thenReturn(cartItem);
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse result = cartService.addItem("user@test.com", request);

        assertNotNull(result);
        assertEquals(3, cartItem.getQuantity()); // 2 + 1
    }

    @Test
    void addItem_InsufficientStock_ThrowsException() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(20); // more than stock (10)

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ApiException.class, () -> cartService.addItem("user@test.com", request));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItem_ProductNotFound_ThrowsException() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(99L);
        request.setQuantity(1);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem("user@test.com", request));
    }

    @Test
    void updateItem_Success() {
        CartItemRequest request = new CartItemRequest();
        request.setQuantity(3);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any())).thenReturn(cartItem);
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        CartResponse result = cartService.updateItem("user@test.com", 1L, request);

        assertNotNull(result);
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    void updateItem_NotOwner_ThrowsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").role(User.Role.CUSTOMER).build();
        CartItemRequest request = new CartItemRequest();
        request.setQuantity(1);

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem)); // belongs to user id=1

        assertThrows(ApiException.class, () -> cartService.updateItem("other@test.com", 1L, request));
    }

    @Test
    void removeItem_Success() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        cartService.removeItem("user@test.com", 1L);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void removeItem_NotOwner_ThrowsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").role(User.Role.CUSTOMER).build();
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        assertThrows(ApiException.class, () -> cartService.removeItem("other@test.com", 1L));
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void clearCart_Success() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        cartService.clearCart("user@test.com");

        verify(cartItemRepository).deleteByUserId(1L);
    }
}
