package com.dogukan.ecommerce.user.dtos;

import java.util.List;

public record ListWithTotalResponse<T>(
        List<T> items,
        long totalCount
) {}
