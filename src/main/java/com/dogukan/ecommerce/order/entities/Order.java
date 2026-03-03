package com.dogukan.ecommerce.order.entities;

import com.dogukan.ecommerce.common.audit.AuditableEntity;
import com.dogukan.ecommerce.order.dtos.OrderStatus;
import com.dogukan.ecommerce.user.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_order_number", columnList = "order_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_order_number", columnNames = "order_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "customer_id",nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items=new ArrayList<>();


    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }
}
