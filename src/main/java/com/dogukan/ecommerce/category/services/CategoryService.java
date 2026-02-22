package com.dogukan.ecommerce.category.services;

import com.dogukan.ecommerce.category.dtos.*;
import com.dogukan.ecommerce.category.entities.Category;
import com.dogukan.ecommerce.category.mapper.CategoryMapper;
import com.dogukan.ecommerce.category.repositories.CategoryQueryRepository;
import com.dogukan.ecommerce.category.repositories.CategoryRepository;
import com.dogukan.ecommerce.common.exception.ApiException;
import com.dogukan.ecommerce.security.SecurityUtils;
import com.dogukan.ecommerce.user.dtos.ListWithTotalResponse;
import com.dogukan.ecommerce.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryQueryRepository categoryQueryRepository;
    private final CategoryMapper mapper;
    private final UserRepository userRepository;


    @Transactional
    public CategoryResponse create(CategoryCreateRequest req) {
        String name = req.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict(

                    "CATEGORY_NAME_EXISTS",
                    "Bu kategori adı zaten mevcut.",
                    Map.of("field", "name")
            );
        }

        Category c = Category.builder()
                .name(name)
                .description(StringUtils.hasText(req.description()) ? req.description().trim() : null)
                .active(true)
                .build();

        Category saved = categoryRepository.save(c);

        // joinli dönmek için
        Category detailed = categoryRepository.findWithAuditById(saved.getId()).orElse(saved);
        return mapper.toResponse(detailed);
    }


    public ListWithTotalResponse<CategoryListItemResponse> search(String q,Boolean active, int page, int size){
        Pageable pageable=PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id"));
        CategoryFilter filter = new CategoryFilter(q, active);

        Page<Category> result = categoryQueryRepository.search(filter, pageable);
        List<CategoryListItemResponse> data = result.getContent().stream()
                .map(mapper::toListItem)
                .toList();
        return new ListWithTotalResponse<>(data, result.getTotalElements());

    }

    public CategoryResponse getById(Long id) {
        Category c = categoryRepository.findWithAuditById(id)
                .orElseThrow(() -> ApiException.notFound(
                        "CATEGORY_NOT_FOUND",
                        "Kategori bulunamadı.",
                        Map.of("id", id)
                ));

        return mapper.toResponse(c);
    }


    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        "CATEGORY_NOT_FOUND",
                        "Kategori bulunamadı.",
                        Map.of("id", id)
                ));

        String newName = req.name().trim();

        if (!newName.equalsIgnoreCase(c.getName()) && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw ApiException.conflict(
                    "CATEGORY_NAME_EXISTS",
                    "Bu kategori adı zaten mevcut.",
                    Map.of("field", "name")
            );
        }

        c.setName(newName);
        c.setDescription(StringUtils.hasText(req.description()) ? req.description().trim() : null);

        if (req.active() != null) {
            c.setActive(req.active());
        }

        Category saved = categoryRepository.save(c);
        Category detailed = categoryRepository.findWithAuditById(saved.getId()).orElse(saved);
        return mapper.toResponse(detailed);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound(
                        "CATEGORY_NOT_FOUND",
                        "Kategori bulunamadı.",
                        Map.of("id", id)
                ));
        Long userId = SecurityUtils.requireCurrentUserId();
        if (userId == null) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Authentication required",
                    Map.of()
            );
        }

        c.setDeletedAt(java.time.Instant.now());
        c.setDeletedBy(this.userRepository.getReferenceById(userId));
        c.setActive(false);
        c.setDeleted(true);
        categoryRepository.save(c);
    }
}
