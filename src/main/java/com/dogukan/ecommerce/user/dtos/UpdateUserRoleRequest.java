package com.dogukan.ecommerce.user.dtos;


import com.dogukan.ecommerce.user.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull Role role
) {}