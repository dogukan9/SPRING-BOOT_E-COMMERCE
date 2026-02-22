package com.dogukan.ecommerce.user.dtos;

import java.time.Instant;
import java.util.Set;

public record UserListItemResponse(
        Long id,
        String username,
        String email,
        String fullName,
        boolean enabled,
        Set<String> roles,
        Instant createdAt,
        UserSummary createdBy
) {}