package com.ecommerce.service;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String userEmail, boolean isAdmin) {
        User user = getUserByEmail(userEmail);

        List<Order> orders;
        if (isAdmin) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String userEmail, Long orderId, boolean isAdmin) {
        User user = getUserByEmail(userEmail);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!isAdmin && !order.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this order");
        }

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse createOrder(String userEmail, OrderRequest request) {
        User user = getUserByEmail(userEmail);

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        // Validate stock and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStockQuantity());
            }
            totalAmount = totalAmount.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        // Create the order
        Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .build();

        // Create order items and reduce stock
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteByUserId(user.getId());

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid order status: " + status);
        }

        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(itemResponses)
                .build();
    }
}
