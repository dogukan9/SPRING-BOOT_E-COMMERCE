package com.dogukan.ecommerce.user.dtos;

import jakarta.validation.constraints.NotNull;

public record UpdateUserEnabledRequest(
        @NotNull Boolean enabled
) {}