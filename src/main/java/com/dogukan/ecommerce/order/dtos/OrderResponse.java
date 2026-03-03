package com.dogukan.ecommerce.order.dtos;
import com.dogukan.ecommerce.user.dtos.UserSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        int itemCount,
        UserSummary customer,
        Instant createdAt,
        Instant updatedAt,
        UserSummary createdBy,
        UserSummary updatedBy,
        List<OrderItemResponse> items
) {}