package com.dogukan.ecommerce.user.dtos;
import java.util.Set;

public record RegisterResponse(

        Long userId,
        String username,
        String fullName,
        Set<String> roles
) {
}
