package com.dogukan.ecommerce.order.dtos;

import com.dogukan.ecommerce.user.dtos.UserSummary;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderListItemResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        int itemCount,
        UserSummary customer,
        Instant createdAt
) {}