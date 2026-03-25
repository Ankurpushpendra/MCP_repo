package com.ecommerce.controller.web;

import com.ecommerce.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
public class OrderWebController {

    private final OrderService orderService;

    public OrderWebController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String listOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("orders", orderService.getOrders(userDetails.getUsername(), isAdmin));
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            Model model) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("order", orderService.getOrderById(userDetails.getUsername(), id, isAdmin));
        return "orders/detail";
    }
}
