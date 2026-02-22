package com.dogukan.ecommerce.common.api;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        Map<String,Object> details
) {
}
