package com.dogukan.ecommerce.security.jwt;

import java.util.Set;

public record JwtPrincipal(
        Long userId,
        String username,
        String fullName,
        Set<String> roles
) {

}
