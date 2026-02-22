package com.dogukan.ecommerce.product.mapper;

import com.dogukan.ecommerce.product.dtos.ProductListItemResponse;
import com.dogukan.ecommerce.product.dtos.ProductResponse;
import com.dogukan.ecommerce.product.entities.Product;
import com.dogukan.ecommerce.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final UserMapper userMapper;

    private ProductResponse.CategoryMini toMini(Product p) {
        return new ProductResponse.CategoryMini(p.getCategory().getId(), p.getCategory().getName());
    }

    public ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQuantity(),
                p.isActive(),
                toMini(p),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                userMapper.toSummary(p.getCreatedBy())
        );
    }

    public ProductListItemResponse toListItem(Product p) {
        return new ProductListItemResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getStockQuantity(),
                p.isActive(),
                toMini(p),
                p.getCreatedAt(),
                userMapper.toSummary(p.getCreatedBy())
        );
    }
}
