package com.dogukan.ecommerce.category.entities;

import com.dogukan.ecommerce.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "categories",
uniqueConstraints = {@UniqueConstraint(name = "uk_categories_name",columnNames = "name")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category  extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
