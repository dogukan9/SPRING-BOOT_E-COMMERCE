package com.dogukan.ecommerce.order.mapper;


import com.dogukan.ecommerce.order.dtos.OrderItemResponse;
import com.dogukan.ecommerce.order.dtos.OrderListItemResponse;
import com.dogukan.ecommerce.order.dtos.OrderResponse;
import com.dogukan.ecommerce.order.entities.Order;
import com.dogukan.ecommerce.order.entities.OrderItem;
import com.dogukan.ecommerce.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final UserMapper userMapper;

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItemCount(),
                userMapper.toSummary(order.getCustomer()),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                userMapper.toSummary(order.getCreatedBy()),
                userMapper.toSummary(order.getUpdatedBy()),
                items
        );
    }

    public OrderListItemResponse toListItem(Order order) {
        return new OrderListItemResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItemCount(),
                userMapper.toSummary(order.getCustomer()),
                order.getCreatedAt()
        );
    }
}