package com.dogukan.ecommerce.user.dtos;

import com.dogukan.ecommerce.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Array;

import java.util.List;
import java.util.Set;

public record RegisterRequest(
        @NotBlank @Size(min=3,max=64) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName,
        @NotEmpty Set<Role> roles
) {
}
