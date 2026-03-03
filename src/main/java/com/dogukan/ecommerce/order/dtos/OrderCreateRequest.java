package com.dogukan.ecommerce.order.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty List<@Valid OrderCreateItemRequest> items
) {}