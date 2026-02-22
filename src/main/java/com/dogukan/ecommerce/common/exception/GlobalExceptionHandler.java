package com.dogukan.ecommerce.common.exception;

import com.dogukan.ecommerce.common.api.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>(ex.getDetails());
        details.put("path", req.getRequestURI());
        details.put("method", req.getMethod());

        return ResponseEntity.status(ex.getStatus()).body(ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage()) // SAFE message
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    private Map<String, Object> baseDetails(HttpServletRequest req) {
        return new HashMap<>(Map.of(
                "path", req.getRequestURI(),
                "method", req.getMethod()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = baseDetails(req);
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));
        details.put("fields", fields);

        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        // Burada ex.getMessage() sızdırabilir; o yüzden sanitize
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, Object> details = baseDetails(req);

        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParse(HttpMessageNotReadableException ex, HttpServletRequest req) {
        // Enum değerleri, class isimleri vs. sızdırma
        log.warn("JSON parse error: {}", ex.getMessage());

        Map<String, Object> details = baseDetails(req);

        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .code("BAD_REQUEST")
                .message("Invalid request body")
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        // SQL, constraint isimleri, tablo/kolon detayları sızdırma
        log.warn("Data integrity violation: {}", ex.getMessage());

        Map<String, Object> details = baseDetails(req);

        // İstersen burada root cause'a bakıp "duplicate" gibi daha iyi mesaj da üretebilirsin
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                .code("CONFLICT")
                .message("Request violates data constraints")
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        Map<String, Object> details = baseDetails(req);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .code("UNAUTHORIZED")
                .message("Invalid username or password")
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        Map<String, Object> details = baseDetails(req);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .code("FORBIDDEN")
                .message("You don't have permission to perform this action")
                .timestamp(Instant.now())
                .details(details)
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest req) {
        // Detayları log’a bas, client’a generic dön
        log.error("Unhandled runtime exception", ex);

        Map<String, Object> details = baseDetails(req);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Unexpected error occurred")
                .timestamp(Instant.now())
                .details(details)
                .build());
    }




}
