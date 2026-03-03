package com.dogukan.ecommerce.order.repositories;

import com.dogukan.ecommerce.order.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select distinct o from Order o
        left join fetch o.customer c
        left join fetch o.createdBy cb
        left join fetch o.updatedBy ub
        left join fetch o.items i
        where o.id = :id and o.deletedAt is null
    """)
    Optional<Order> findWithDetailsById(Long id);

    Optional<Order> findByIdAndDeletedAtIsNull(Long id);
}
