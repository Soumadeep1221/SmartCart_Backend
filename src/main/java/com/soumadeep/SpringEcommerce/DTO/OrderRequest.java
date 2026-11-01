package com.soumadeep.SpringEcommerce.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(@NotBlank(message = "Customer name is required") String customerName,
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items) {}