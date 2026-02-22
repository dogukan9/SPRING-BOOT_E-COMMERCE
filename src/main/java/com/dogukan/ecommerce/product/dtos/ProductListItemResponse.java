package com.dogukan.ecommerce.product.dtos;

import com.dogukan.ecommerce.user.dtos.UserSummary;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductListItemResponse(
        Long id,
        String name,
        BigDecimal price,
        int stockQuantity,
        boolean active,
        ProductResponse.CategoryMini category,
        Instant createdAt,
        UserSummary createdBy
) {}