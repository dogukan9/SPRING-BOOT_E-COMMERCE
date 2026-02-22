package com.dogukan.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final Map<String, Object> details;

    public ApiException(HttpStatus status, String code, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? Map.of() : details;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public Map<String, Object> getDetails() { return details; }

    // Factory helpers
    public static ApiException badRequest(String code, String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message, details);
    }
    public static ApiException notFound(String code, String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message, details);
    }
    public static ApiException conflict(String code, String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.CONFLICT, code, message, details);
    }
}
