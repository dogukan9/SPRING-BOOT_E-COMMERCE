package com.dogukan.ecommerce.user.dtos;

public record UserSummary(
        Long id,
        String username,
        String fullName
) { }
