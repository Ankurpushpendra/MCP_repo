package com.ecommerce.controller.web;

import com.ecommerce.dto.request.CategoryRequest;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWebController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;

    public AdminWebController(ProductService productService, CategoryService categoryService,
                               UserService userService, OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.getAllProducts(0, Integer.MAX_VALUE, null, null).getTotalElements());
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        return "admin/dashboard";
    }

    // --- Products ---

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.getAllProducts(0, Integer.MAX_VALUE, null, null).getContent());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new ProductRequest());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String createProduct(
            @Valid @ModelAttribute("product") ProductRequest request,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product-form";
        }
        try {
            productService.createProduct(request);
            redirectAttributes.addFlashAttribute("success", "Product created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        var product = productService.getProductById(id);
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setStockQuantity(product.getStockQuantity());
        request.setCategoryId(product.getCategoryId());
        request.setImageUrl(product.getImageUrl());
        model.addAttribute("product", request);
        model.addAttribute("productId", id);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/{id}/update")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductRequest request,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product-form";
        }
        try {
            productService.updateProduct(id, request);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // --- Categories ---

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new CategoryRequest());
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String createCategory(
            @Valid @ModelAttribute("category") CategoryRequest request,
            BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "admin/category-form";
        try {
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("success", "Category created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        var cat = categoryService.getCategoryById(id);
        CategoryRequest request = new CategoryRequest();
        request.setName(cat.getName());
        request.setDescription(cat.getDescription());
        model.addAttribute("category", request);
        model.addAttribute("categoryId", id);
        return "admin/category-form";
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") CategoryRequest request,
            BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "admin/category-form";
        try {
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // --- Users ---

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    // --- Orders ---

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
