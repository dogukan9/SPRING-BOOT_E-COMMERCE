package com.dogukan.ecommerce.common.api;

import lombok.Builder;

@Builder
public record ApiResponse<T>(
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .code("OK")
                .message(message)
                .data(data)
                .build();
    }
}
