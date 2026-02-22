package com.dogukan.ecommerce.user.service;

import com.dogukan.ecommerce.common.exception.ApiException;
import com.dogukan.ecommerce.user.dtos.*;
import com.dogukan.ecommerce.user.entities.User;
import com.dogukan.ecommerce.user.enums.Role;
import com.dogukan.ecommerce.user.mapper.UserMapper;
import com.dogukan.ecommerce.user.repositories.UserQueryRepository;
import com.dogukan.ecommerce.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final UserMapper mapper;
    private final UserQueryRepository userQueryRepository;

    public UserResponse me(User current){
        return mapper.toResponse(current);
    }

    @Transactional
    public UserResponse createByAdmin(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Email already exists");
        }

        // Admin endpointinden ADMIN rolü verilmesini engelle
        if (req.role() == Role.ADMIN) {
            throw new RuntimeException("Cannot assign ADMIN role via this endpoint");
        }

        boolean enabled = req.enabled() == null || req.enabled();

        User user = User.builder()
                .username(req.username().trim())
                .email(req.email().trim().toLowerCase())
                .password(encoder.encode(req.password()))
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .roles(Set.of(req.role()))
                .enabled(enabled)
                .build();

        User saved = userRepository.save(user);
        User detailed = userRepository.findWithAuditById(saved.getId()).orElse(saved);
        return mapper.toResponse(detailed);
    }


public ListWithTotalResponse<UserListItemResponse> searchUsers(String q, Role role, Boolean enabled, int page, int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    UserFilter filter = new UserFilter(q, role, enabled);

    Page<User> users = userQueryRepository.searchUsers(filter, pageable);

    List<UserListItemResponse> data = users.getContent().stream()
            .map(mapper::toListItem)
            .toList();

    return new ListWithTotalResponse<>(data, users.getTotalElements());
}

    public UserResponse getById(Long id){
        User u=userRepository.findWithAuditById(id)
                .orElseThrow(()->new RuntimeException("User not found"));
        return mapper.toResponse(u);
    }


    @Transactional
    public UserResponse updateRole(Long id, UpdateUserRoleRequest req) {

        if (req.role() == Role.ADMIN) {
                   throw ApiException.conflict(
               "!",
                "Cannot assign ADMIN role via this endpoint.",
                Map.of());

        }
        System.out.println(req.role());
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

//        u.setRoles(Set.of(req.role()));
        u.setRoles(new java.util.HashSet<>(java.util.Set.of(req.role())));

        User saved = userRepository.save(u);

        User detailed = userRepository.findWithAuditById(saved.getId()).orElse(saved);
        return mapper.toResponse(detailed);
    }


    @Transactional
    public UserResponse updateEnabled(Long id, UpdateUserEnabledRequest req) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        u.setEnabled(req.enabled());
        User saved = userRepository.save(u);

        User detailed = userRepository.findWithAuditById(saved.getId()).orElse(saved);
        return mapper.toResponse(detailed);
    }
}
