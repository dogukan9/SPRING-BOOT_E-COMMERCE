package com.dogukan.ecommerce.product.dtos;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank @Size(min = 2, max = 180) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @Min(0) int stockQuantity,
        @NotNull Long categoryId,
        Boolean active
) {}
