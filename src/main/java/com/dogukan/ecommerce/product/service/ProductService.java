package com.dogukan.ecommerce.product.service;

import com.dogukan.ecommerce.category.entities.Category;
import com.dogukan.ecommerce.category.repositories.CategoryRepository;
import com.dogukan.ecommerce.common.exception.ApiException;
import com.dogukan.ecommerce.product.dtos.*;
import com.dogukan.ecommerce.product.entities.Product;
import com.dogukan.ecommerce.product.mapper.ProductMapper;
import com.dogukan.ecommerce.product.repositories.ProductQueryRepository;
import com.dogukan.ecommerce.product.repositories.ProductRepository;
import com.dogukan.ecommerce.security.SecurityUtils;
import com.dogukan.ecommerce.user.dtos.ListWithTotalResponse;
import com.dogukan.ecommerce.user.enums.Role;
import com.dogukan.ecommerce.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository; // getReferenceById için
    private final ProductMapper mapper;

    @Transactional
    public ProductResponse create(ProductCreateRequest req){
        Category cat=categoryRepository.findById(req.categoryId())
                .orElseThrow(()-> ApiException.notFound(
                        "Category_not_found",
                        "Kategori bulunamadı",
                        Map.of("categoryId",req.categoryId())
                )
                );

        Product p = Product.builder()
                .name(req.name().trim())
                .description(StringUtils.hasText(req.description()) ? req.description().trim() : null)
                .price(req.price())
                .stockQuantity(req.stockQuantity())
                .active(req.active() == null || req.active())
                .category(cat)
                .build();

        Product saved = productRepository.save(p);
        Product detailed = productRepository.findWithAuditById(saved.getId()).orElse(saved);
        return mapper.toResponse(detailed);
    }

    public ListWithTotalResponse<ProductListItemResponse> search(
            String q,
            Long categoryId,
            Boolean active,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            Long createdById,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        ProductFilter filter = new ProductFilter(q, categoryId, active, minPrice, maxPrice, createdById);

        Page<Product> result = productQueryRepository.search(filter, pageable);

        List<ProductListItemResponse> data = result.getContent().stream()
                .map(mapper::toListItem)
                .toList();

        return new ListWithTotalResponse<>(data, result.getTotalElements());
    }

    public ProductResponse getById(Long id) {
        Product p = productRepository.findWithAuditById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Ürün bulunamadı.",
                        Map.of("id", id)
                ));

        return mapper.toResponse(p);
    }



    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest req, Authentication auth) {
        Product p = productRepository.findWithAuditById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Ürün bulunamadı.",
                        Map.of("id", id)
                ));

        enforceSellerOwnershipIfNeeded(p, auth);

        if (p.getVersion() == null || !p.getVersion().equals(req.version())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "STALE_PRODUCT",
                    "Ürün başka biri tarafından güncellendi. Lütfen sayfayı yenileyip tekrar deneyin.",
                    Map.of(
                            "currentVersion", p.getVersion(),
                            "requestVersion", req.version()
                    )
            );
        }

        Category cat = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Kategori bulunamadı.",
                        Map.of("categoryId", req.categoryId())
                ));

        p.setName(req.name().trim());
        p.setDescription(StringUtils.hasText(req.description()) ? req.description().trim() : null);
        p.setPrice(req.price());
        p.setStockQuantity(req.stockQuantity());
        p.setCategory(cat);

        if (req.active() != null) {
            p.setActive(req.active());
        }

        Product saved = productRepository.save(p);
        Product detailed = productRepository.findWithAuditById(saved.getId()).orElse(saved);
        return mapper.toResponse(detailed);
    }


    @Transactional
    public void softDelete(Long id, Authentication auth) {
        Product p = productRepository.findWithAuditById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Ürün bulunamadı.",
                        Map.of("id", id)
                ));

        enforceSellerOwnershipIfNeeded(p, auth);

        Long actorId = SecurityUtils.requireCurrentUserId();
        if (actorId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required", Map.of());
        }

        p.setDeletedAt(Instant.now());
        p.setDeleted(true);
        p.setDeletedBy(userRepository.getReferenceById(actorId));
        p.setActive(false);
        productRepository.save(p);
    }

    private void enforceSellerOwnershipIfNeeded(Product p, Authentication auth) {
        if (auth == null) return;

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
        if (isAdmin) return;

        boolean isSeller = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.SELLER.name()));
        if (!isSeller) {
            // USER gibi bir rol update/delete yapmaya çalışırsa
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bu işlem için yetkiniz yok.", Map.of());
        }

        Long userId = SecurityUtils.requireCurrentUserId();
        Long ownerId = (p.getCreatedBy() != null ? p.getCreatedBy().getId() : null);

        if (userId == null || ownerId == null || !ownerId.equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Sadece kendi ürününüzü güncelleyebilirsiniz.", Map.of());
        }
    }

}
