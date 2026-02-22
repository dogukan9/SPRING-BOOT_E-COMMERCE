package com.dogukan.ecommerce.product.repositories;

import com.dogukan.ecommerce.product.dtos.ProductFilter;
import com.dogukan.ecommerce.product.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductQueryRepository {
    Page<Product> search(ProductFilter filter, Pageable pageable);

}
