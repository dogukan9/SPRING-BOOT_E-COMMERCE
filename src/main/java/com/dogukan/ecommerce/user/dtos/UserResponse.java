package com.dogukan.ecommerce.user.dtos;


import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String fullName,
        boolean enabled,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt,
        UserSummary createdBy,
        UserSummary updatedBy
) {}