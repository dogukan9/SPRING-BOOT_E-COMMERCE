package com.dogukan.ecommerce.user.mapper;
import com.dogukan.ecommerce.user.dtos.UserListItemResponse;
import com.dogukan.ecommerce.user.dtos.UserResponse;
import com.dogukan.ecommerce.user.dtos.UserSummary;
import com.dogukan.ecommerce.user.entities.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserSummary toSummary(User u){
        if(u==null) return null;
        return new UserSummary(u.getId(), u.getUsername(),u.getFullName());
    }

    public UserResponse toResponse(User u){
        if(u==null) return null;
        Set<String> roles=u.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getFullName(),
                u.isEnabled(),
                roles,
                u.getCreatedAt(),
                u.getUpdatedAt(),
                toSummary(u.getCreatedBy()),
                toSummary(u.getUpdatedBy())
        );
    }


    public UserListItemResponse toListItem(User u) {
        if (u == null) return null;

        Set<String> roles = u.getRoles().stream().map(Enum::name).collect(Collectors.toSet());

        return new UserListItemResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFullName(),
                u.isEnabled(),
                roles,
                u.getCreatedAt(),
                toSummary(u.getCreatedBy())
        );
    }
}
