package com.dogukan.ecommerce.user.dtos;
import java.time.Instant;
import java.util.Set;

public record AuthResponse(
        String accessToken
//        Instant expiresAt,
//        Long userId,
//        String username,
//        String fullName,
//        Set<String> roles
) { }
