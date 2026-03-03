package com.dogukan.ecommerce.user.controller;

import com.dogukan.ecommerce.common.api.ApiResponse;
import com.dogukan.ecommerce.security.jwt.JwtPrincipal;
import com.dogukan.ecommerce.user.dtos.*;
import com.dogukan.ecommerce.user.dtos.*;
import com.dogukan.ecommerce.user.enums.Role;
import com.dogukan.ecommerce.user.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UsersService userService;

    @GetMapping("/me")
    public ApiResponse<?> me(@AuthenticationPrincipal JwtPrincipal current) {
        return ApiResponse.ok("Me", current);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ApiResponse.ok("User created", userService.createByAdmin(req));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ListWithTotalResponse<UserListItemResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok("Users", userService.searchUsers(q, role, enabled, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok("User", userService.getById(id));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest req) {
        return ApiResponse.ok("Role is updated", userService.updateRole(id, req));
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateEnabled(@PathVariable Long id, @Valid @RequestBody UpdateUserEnabledRequest req) {
        return ApiResponse.ok("Enabled updated", userService.updateEnabled(id, req));
    }
}
