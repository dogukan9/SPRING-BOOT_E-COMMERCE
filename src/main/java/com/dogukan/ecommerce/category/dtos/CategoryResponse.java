package com.dogukan.ecommerce.category.dtos;

import com.dogukan.ecommerce.user.dtos.UserSummary;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        UserSummary createdBy){}