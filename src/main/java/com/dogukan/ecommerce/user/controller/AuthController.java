package com.dogukan.ecommerce.user.controller;



import com.dogukan.ecommerce.common.api.ApiResponse;
import com.dogukan.ecommerce.user.dtos.AuthResponse;
import com.dogukan.ecommerce.user.dtos.LoginRequest;
import com.dogukan.ecommerce.user.dtos.RegisterRequest;
import com.dogukan.ecommerce.user.dtos.RegisterResponse;
import com.dogukan.ecommerce.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("Registered", authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Logged in", authService.login(req));
    }
}
