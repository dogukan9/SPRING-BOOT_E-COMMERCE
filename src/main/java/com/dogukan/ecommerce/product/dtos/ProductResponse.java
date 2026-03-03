package com.dogukan.ecommerce.product.dtos;


import com.dogukan.ecommerce.user.dtos.UserSummary;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        Long version,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        boolean active,
        CategoryMini category,
        Instant createdAt,
        Instant updatedAt,
        UserSummary createdBy
) {
    public record CategoryMini(Long id, String name) {}
}