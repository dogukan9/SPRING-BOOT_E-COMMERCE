package com.dogukan.ecommerce.category.repositories;


import com.dogukan.ecommerce.category.dtos.CategoryFilter;
import com.dogukan.ecommerce.category.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryQueryRepository {
    Page<Category> search(CategoryFilter filter, Pageable pageable);
}