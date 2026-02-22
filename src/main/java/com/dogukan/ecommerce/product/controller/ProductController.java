package com.dogukan.ecommerce.product.controller;


import com.dogukan.ecommerce.common.api.ApiResponse;
import com.dogukan.ecommerce.product.dtos.*;
import com.dogukan.ecommerce.product.service.ProductService;
import com.dogukan.ecommerce.user.dtos.ListWithTotalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ADMIN + SELLER
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        return ApiResponse.ok("Product created", productService.create(req));
    }

    // ALL AUTHENTICATED (senin security config anyRequest authenticated ise)
    @GetMapping
    public ApiResponse<ListWithTotalResponse<ProductListItemResponse>> search(
            @RequestParam(name = "search",required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long createdById,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok("Products",
                productService.search(search, categoryId, active, minPrice, maxPrice, createdById, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok("Product", productService.getById(id));
    }

    // ADMIN + SELLER (ownership check service içinde)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest req, Authentication auth) {
        return ApiResponse.ok("Product updated", productService.update(id, req, auth));
    }

    // ADMIN + SELLER (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        productService.softDelete(id, auth);
        return ApiResponse.ok("Product deleted", null);
    }
}
