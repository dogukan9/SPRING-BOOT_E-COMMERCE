package com.dogukan.ecommerce.order.dtos;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderSearchRequest(
        String q,
        OrderStatus status,
        Long customerId,
        BigDecimal minTotal,
        BigDecimal maxTotal,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size
) {}