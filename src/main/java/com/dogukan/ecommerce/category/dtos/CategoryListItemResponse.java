package com.dogukan.ecommerce.category.dtos;

import com.dogukan.ecommerce.user.dtos.UserSummary;

import java.time.Instant;

public record CategoryListItemResponse(
        Long id,
        String name,
        boolean active,
        Instant createdAt,
        UserSummary createdBy
) {}