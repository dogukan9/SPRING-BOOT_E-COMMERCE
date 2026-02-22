package com.dogukan.ecommerce.user.repositories;


import com.dogukan.ecommerce.user.dtos.UserFilter;
import com.dogukan.ecommerce.user.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {
    Page<User> searchUsers(UserFilter filter, Pageable pageable);
}
