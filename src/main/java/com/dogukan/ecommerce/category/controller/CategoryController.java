package com.dogukan.ecommerce.category.controller;


import com.dogukan.ecommerce.category.dtos.*;
import com.dogukan.ecommerce.category.services.CategoryService;
import com.dogukan.ecommerce.common.api.ApiResponse;
import com.dogukan.ecommerce.user.dtos.ListWithTotalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ApiResponse.ok("Category created", categoryService.create(req));
    }

    @GetMapping
    public ApiResponse<ListWithTotalResponse<CategoryListItemResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok("Categories", categoryService.search(q, active, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok("Category", categoryService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest req) {
        return ApiResponse.ok("Category updated", categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        categoryService.delete(id,authentication);
        return ApiResponse.ok("Category deleted", null);
    }
}
