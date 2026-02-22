package com.dogukan.ecommerce.category.repositories;
import com.dogukan.ecommerce.category.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query("""
        select c from Category c
        left join fetch c.createdBy cb
        where c.id = :id and c.deleted = false
    """)
    Optional<Category> findWithAuditById(Long id);
}
