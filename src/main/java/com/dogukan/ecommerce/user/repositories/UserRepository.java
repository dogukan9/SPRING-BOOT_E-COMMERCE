package com.dogukan.ecommerce.user.repositories;

import com.dogukan.ecommerce.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query(
            """
                    select u from User u
                    left join fetch u.createdBy cb
                    left join fetch u.updatedBy ub
                    where u.id = :id
                    """
    )
    Optional<User> findWithAuditById(Long id);
}