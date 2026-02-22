package com.dogukan.ecommerce.user.dtos;

import com.dogukan.ecommerce.user.enums.Role;

public record UserFilter(
        String q,      // username/email/name like
        Role role,     // role filter
        Boolean enabled
) { }
