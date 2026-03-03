package com.dogukan.ecommerce.product.repositories;

import com.dogukan.ecommerce.product.entities.Product;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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


    // Order create sırasında stok düşmek için
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select p from Product p
    where p.id in :ids
      and p.deletedAt is null
      and p.active = true
""")
    java.util.List<com.dogukan.ecommerce.product.entities.Product> findAllAvailableByIdsForUpdate(@Param("ids") java.util.List<Long> ids);

    // Cancel sırasında stok iadesi için
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select p from Product p
    where p.id in :ids
""")
    java.util.List<com.dogukan.ecommerce.product.entities.Product> findAllByIdsForUpdate(@Param("ids") java.util.List<Long> ids);
}
