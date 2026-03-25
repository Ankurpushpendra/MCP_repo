package com.ecommerce.service;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private OrderService orderService;

    private User user;
    private Product product;
    private CartItem cartItem;
    private Order order;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@test.com").role(User.Role.CUSTOMER).build();
        product = Product.builder()
                .id(1L).name("Laptop").price(new BigDecimal("999.99")).stockQuantity(10)
                .category(Category.builder().id(1L).name("Electronics").build())
                .build();
        cartItem = CartItem.builder().id(1L).user(user).product(product).quantity(2).build();

        order = Order.builder()
                .id(1L).user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1999.98"))
                .shippingAddress("123 Main St")
                .orderItems(new ArrayList<>())
                .build();
    }

    @Test
    void getOrders_AsUser_ReturnsUserOrders() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getOrders("user@test.com", false);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getOrders_AsAdmin_ReturnsAllOrders() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(
                User.builder().id(2L).email("admin@test.com").role(User.Role.ADMIN).build()));
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getOrders("admin@test.com", true);

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
        verify(orderRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    void getOrderById_Found_ReturnsOrder() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById("user@test.com", 1L, false);

        assertEquals(1L, result.getId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById("user@test.com", 99L, false));
    }

    @Test
    void getOrderById_Forbidden_ThrowsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").role(User.Role.CUSTOMER).build();
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order)); // order belongs to user id=1

        assertThrows(ApiException.class,
                () -> orderService.getOrderById("other@test.com", 1L, false));
    }

    @Test
    void createOrder_Success() {
        OrderRequest request = new OrderRequest();
        request.setShippingAddress("123 Main St");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(productRepository.save(any())).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.createOrder("user@test.com", request);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
        verify(cartItemRepository).deleteByUserId(1L);
    }

    @Test
    void createOrder_EmptyCart_ThrowsException() {
        OrderRequest request = new OrderRequest();
        request.setShippingAddress("123 Main St");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        assertThrows(ApiException.class, () -> orderService.createOrder("user@test.com", request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        product.setStockQuantity(1); // only 1 in stock, cart has 2
        OrderRequest request = new OrderRequest();
        request.setShippingAddress("123 Main St");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        assertThrows(ApiException.class, () -> orderService.createOrder("user@test.com", request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        OrderResponse result = orderService.updateOrderStatus(1L, "PROCESSING");

        assertEquals("PROCESSING", result.getStatus());
    }

    @Test
    void updateOrderStatus_InvalidStatus_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ApiException.class, () -> orderService.updateOrderStatus(1L, "INVALID_STATUS"));
    }

    @Test
    void updateOrderStatus_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(99L, "PROCESSING"));
    }
}
