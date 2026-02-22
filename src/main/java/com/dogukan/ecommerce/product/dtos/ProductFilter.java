package com.dogukan.ecommerce.product.dtos;

import java.math.BigDecimal;

public record ProductFilter(
        String q,
        Long categoryId,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long createdById
) {}
