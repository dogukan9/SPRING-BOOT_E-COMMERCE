package com.dogukan.ecommerce.product.repositories;

import com.dogukan.ecommerce.product.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {

    @Query("""
        select p from Product p
        join fetch p.category cat
        left join fetch p.createdBy cb
        left join fetch p.updatedBy ub
        where p.id = :id and p.deletedAt is null
    """)
    Optional<Product> findWithAuditById(Long id);

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);
    Optional<Product> findByIdAndDeletedIsFalse(Long id);


}
