package com.dogukan.ecommerce.order.contoller;


import com.dogukan.ecommerce.common.api.ApiResponse;
import com.dogukan.ecommerce.order.dtos.*;
import com.dogukan.ecommerce.order.services.OrderService;
import com.dogukan.ecommerce.user.dtos.ListWithTotalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderCreateRequest req) {
        return ApiResponse.ok("Order created", orderService.create(req));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ApiResponse<ListWithTotalResponse<OrderListItemResponse>> myOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok("My orders", orderService.myOrders(status, page, size));
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ListWithTotalResponse<OrderListItemResponse>> search(@RequestBody OrderSearchRequest req) {
        return ApiResponse.ok("Orders", orderService.searchForAdmin(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.ok("Order", orderService.getById(id, authentication));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateOrderStatusRequest req) {
        return ApiResponse.ok("Order status updated", orderService.updateStatus(id, req));
    }
}