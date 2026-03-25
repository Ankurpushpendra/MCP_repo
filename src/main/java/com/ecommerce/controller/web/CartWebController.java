package com.ecommerce.controller.web;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartWebController {

    private final CartService cartService;
    private final OrderService orderService;

    public CartWebController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("cart", cartService.getCart(userDetails.getUsername()));
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes) {

        try {
            CartItemRequest request = new CartItemRequest();
            request.setProductId(productId);
            request.setQuantity(quantity);
            cartService.addItem(userDetails.getUsername(), request);
            redirectAttributes.addFlashAttribute("success", "Item added to cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update/{id}")
    public String updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {

        try {
            CartItemRequest request = new CartItemRequest();
            request.setQuantity(quantity);
            cartService.updateItem(userDetails.getUsername(), id, request);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{id}")
    public String removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.removeItem(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String shippingAddress,
            RedirectAttributes redirectAttributes) {

        try {
            OrderRequest request = new OrderRequest();
            request.setShippingAddress(shippingAddress);
            var order = orderService.createOrder(userDetails.getUsername(), request);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully!");
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}
