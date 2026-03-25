package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
}
