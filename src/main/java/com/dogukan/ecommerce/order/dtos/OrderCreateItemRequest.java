package com.dogukan.ecommerce.order.dtos;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderCreateItemRequest(
        @NotNull Long productId,
        @Min(1) int quantity
) {}