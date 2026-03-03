package com.dogukan.ecommerce.order.services;

import com.dogukan.ecommerce.common.exception.ApiException;
import com.dogukan.ecommerce.order.dtos.*;
import com.dogukan.ecommerce.order.entities.Order;
import com.dogukan.ecommerce.order.entities.OrderItem;
import com.dogukan.ecommerce.order.mapper.OrderMapper;
import com.dogukan.ecommerce.order.repositories.OrderQueryRepository;
import com.dogukan.ecommerce.order.repositories.OrderRepository;
import com.dogukan.ecommerce.product.entities.Product;
import com.dogukan.ecommerce.product.repositories.ProductRepository;
import com.dogukan.ecommerce.security.SecurityUtils;
import com.dogukan.ecommerce.user.dtos.ListWithTotalResponse;
import com.dogukan.ecommerce.user.entities.User;
import com.dogukan.ecommerce.user.enums.Role;
import com.dogukan.ecommerce.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper mapper;

    @Transactional
    public OrderResponse create(OrderCreateRequest req) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        validateCreateRequest(req);
        List<Long> productIds = req.items().stream()
                .map(OrderCreateItemRequest::productId)
                .toList();

        List<Product> products = productRepository.findAllAvailableByIdsForUpdate(productIds);

        if (products.size() != productIds.stream().distinct().count()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PRODUCTS",
                    "Bir veya daha fazla ürün bulunamadı ya da sipariş verilemez durumda.",
                    Map.of()
            );
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        User customerRef = userRepository.getReferenceById(currentUserId);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customerRef)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .itemCount(0)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderCreateItemRequest itemReq : req.items()) {
            Product product = productMap.get(itemReq.productId());
            if (product == null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "PRODUCT_NOT_FOUND",
                        "Ürün bulunamadı.",
                        Map.of("productId", itemReq.productId())
                );
            }

            if (product.getStockQuantity() < itemReq.quantity()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "INSUFFICIENT_STOCK",
                        "Ürün stoğu yetersiz.",
                        Map.of("productId", product.getId(), "availableStock", product.getStockQuantity())
                );
            }

            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemReq.quantity())
                    .lineTotal(lineTotal)
                    .build();

            order.addItem(orderItem);

            totalAmount = totalAmount.add(lineTotal);
            totalQuantity += itemReq.quantity();
        }

        order.setTotalAmount(totalAmount);
        order.setItemCount(totalQuantity);

        Order saved = orderRepository.save(order);
        Order detailed = orderRepository.findWithDetailsById(saved.getId()).orElse(saved);

        return mapper.toResponse(detailed);
    }

    @Transactional(readOnly = true)
    public ListWithTotalResponse<OrderListItemResponse> myOrders(OrderStatus status, int page, int size) {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        OrderFilter filter = new OrderFilter(null, status, currentUserId, null, null, null, null);

        Page<Order> result = orderQueryRepository.search(filter, pageable);

        List<OrderListItemResponse> data = result.getContent().stream()
                .map(mapper::toListItem)
                .toList();

        return new ListWithTotalResponse<>(data, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ListWithTotalResponse<OrderListItemResponse> searchForAdmin(OrderSearchRequest req) {
        int page = req.page() == null ? 0 : req.page();
        int size = req.size() == null ? 20 : req.size();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        OrderFilter filter = new OrderFilter(
                req.q(),
                req.status(),
                req.customerId(),
                req.minTotal(),
                req.maxTotal(),
                req.createdFrom(),
                req.createdTo()
        );

        Page<Order> result = orderQueryRepository.search(filter, pageable);

        List<OrderListItemResponse> data = result.getContent().stream()
                .map(mapper::toListItem)
                .toList();

        return new ListWithTotalResponse<>(data, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id, Authentication authentication) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "ORDER_NOT_FOUND",
                        "Sipariş bulunamadı.",
                        Map.of("id", id)
                ));

        enforceOwnershipOrAdmin(order, authentication);
        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest req) {
        Order order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "ORDER_NOT_FOUND",
                        "Sipariş bulunamadı.",
                        Map.of("id", id)
                ));

        OrderStatus current = order.getStatus();
        OrderStatus target = req.status();

        Long currentUserId = SecurityUtils.requireCurrentUserId();
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;

        if (currentUserId == null || customerId == null || !currentUserId.equals(customerId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Bu siparişi editleme yetkiniz yok.",
                    Map.of()
            );
        }

        validateStatusTransition(current, target);

        if (target == OrderStatus.CANCELLED && current != OrderStatus.CANCELLED) {
            restoreStocks(order);
        }

        order.setStatus(target);

        Order saved = orderRepository.save(order);
        Order detailed = orderRepository.findWithDetailsById(saved.getId()).orElse(saved);

        return mapper.toResponse(detailed);
    }

    private void validateCreateRequest(OrderCreateRequest req) {
        Set<Long> uniqueIds = new HashSet<>();
        for (OrderCreateItemRequest item : req.items()) {
            if (!uniqueIds.add(item.productId())) {
                 throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "DUPLICATE_PRODUCT",
                        "Aynı ürün sipariş içinde birden fazla kez gönderilemez.",
                        Map.of("productId", item.productId())
                );
            }
        }
    }

    private void enforceOwnershipOrAdmin(Order order, Authentication authentication) {
        if (authentication == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required", Map.of());
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()));

        if (isAdmin) {
            return;
        }

        Long currentUserId = SecurityUtils.requireCurrentUserId();
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;

        if (currentUserId == null || customerId == null || !currentUserId.equals(customerId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Bu siparişi görüntüleme yetkiniz yok.",
                    Map.of()
            );
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == target) {
            return;
        }

        switch (current) {
            case PENDING -> {
                if (target != OrderStatus.CONFIRMED && target != OrderStatus.CANCELLED) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION",
                            "PENDING sipariş sadece CONFIRMED veya CANCELLED olabilir.", Map.of());
                }
            }
            case CONFIRMED -> {
                if (target != OrderStatus.SHIPPED && target != OrderStatus.CANCELLED) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION",
                            "CONFIRMED sipariş sadece SHIPPED veya CANCELLED olabilir.", Map.of());
                }
            }
            case SHIPPED -> {
                if (target != OrderStatus.DELIVERED) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION",
                            "SHIPPED sipariş sadece DELIVERED olabilir.", Map.of());
                }
            }
            case DELIVERED, CANCELLED -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_STATUS_TRANSITION",
                    "DELIVERED veya CANCELLED sipariş tekrar değiştirilemez.",
                    Map.of()
            );
        }
    }

    private void restoreStocks(Order order) {
        List<Long> productIds = order.getItems().stream()
                .map(OrderItem::getProductId)
                .distinct()
                .toList();

        List<Product> products = productRepository.findAllByIdsForUpdate(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderItem item : order.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "PRODUCT_NOT_FOUND_FOR_RESTORE",
                        "İptal sırasında stok iadesi yapılamadı.",
                        Map.of("productId", item.getProductId())
                );
            }

            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private String generateOrderNumber() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }
}