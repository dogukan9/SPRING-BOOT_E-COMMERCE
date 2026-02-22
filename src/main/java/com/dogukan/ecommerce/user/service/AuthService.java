package com.dogukan.ecommerce.user.service;

import com.dogukan.ecommerce.common.exception.ApiException;
import com.dogukan.ecommerce.security.ActiveUserStore;
import com.dogukan.ecommerce.security.jwt.JwtService;
import com.dogukan.ecommerce.user.dtos.*;
import com.dogukan.ecommerce.user.entities.User;
import com.dogukan.ecommerce.user.enums.Role;
import com.dogukan.ecommerce.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ActiveUserStore activeUserStore;

    public RegisterResponse register(RegisterRequest req){
        if(userRepository.existsByUsername(req.username())){
            throw ApiException.conflict("Username_Exist","Username already exist", Map.of("username",req.username()));

        }
        if(userRepository.existsByEmail(req.email())){
            throw ApiException.conflict("email_Exist","emial already exist", Map.of("username",req.email()));
        }
        User user=User.builder()
                .username(req.username().trim())
                .email(req.email().trim().toLowerCase())
                .password(encoder.encode(req.password()))
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .roles(req.roles())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getFullName(),
                saved.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }


    public AuthResponse login(LoginRequest req){
        Authentication auth=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(),req.password())
        );

        User user=(User) auth.getPrincipal();
        String token=jwtService.generateAccessToken(user);
        Instant exp=jwtService.getExpirationFromToken(token);
        activeUserStore.markActive(user.getId(), exp);
        return new AuthResponse(
                token
        );
    }
}
