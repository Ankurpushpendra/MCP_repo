package com.ecommerce.controller;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        CartResponse cart = cartService.getCart(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse cart = cartService.addItem(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse cart = cartService.updateItem(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        cartService.removeItem(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
}
