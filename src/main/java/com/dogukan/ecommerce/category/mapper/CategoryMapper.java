package com.dogukan.ecommerce.category.mapper;


import com.dogukan.ecommerce.category.dtos.CategoryListItemResponse;
import com.dogukan.ecommerce.category.dtos.CategoryResponse;
import com.dogukan.ecommerce.category.entities.Category;
import com.dogukan.ecommerce.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final UserMapper userMapper;

    public CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.isActive(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                userMapper.toSummary(c.getCreatedBy())
        );
    }

    public CategoryListItemResponse toListItem(Category c) {
        return new CategoryListItemResponse(
                c.getId(),
                c.getName(),
                c.isActive(),
                c.getCreatedAt(),
                userMapper.toSummary(c.getCreatedBy())
        );
    }
}
