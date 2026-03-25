package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ApiException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        return buildCartResponse(items);
    }

    @Transactional
    public CartResponse addItem(String userEmail, CartItemRequest request) {
        User user = getUserByEmail(userEmail);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock. Available: " + product.getStockQuantity());
        }

        Optional<CartItem> existingItem = cartItemRepository
                .findByUserIdAndProductId(user.getId(), product.getId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock. Available: " + product.getStockQuantity());
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
        }

        cartItemRepository.save(cartItem);

        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        return buildCartResponse(items);
    }

    @Transactional
    public CartResponse updateItem(String userEmail, Long cartItemId, CartItemRequest request) {
        User user = getUserByEmail(userEmail);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this cart item");
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock. Available: " + product.getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        return buildCartResponse(items);
    }

    @Transactional
    public void removeItem(String userEmail, Long cartItemId) {
        User user = getUserByEmail(userEmail);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        cartItemRepository.deleteByUserId(user.getId());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private CartResponse buildCartResponse(List<CartItem> items) {
        List<CartResponse.CartItemResponse> itemResponses = items.stream()
                .map(item -> CartResponse.CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .createdAt(item.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(total)
                .totalItems(totalItems)
                .build();
    }
}
