package com.dogukan.ecommerce.order.repositories;

import com.dogukan.ecommerce.order.dtos.OrderFilter;
import com.dogukan.ecommerce.order.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryRepository {
    Page<Order> search(OrderFilter filter, Pageable pageable);
}
