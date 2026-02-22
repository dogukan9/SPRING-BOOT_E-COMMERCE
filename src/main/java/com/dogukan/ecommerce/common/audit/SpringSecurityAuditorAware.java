package com.dogukan.ecommerce.common.audit;

import com.dogukan.ecommerce.security.jwt.JwtPrincipal;
import com.dogukan.ecommerce.user.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("springSecurityAuditorAware")
public class SpringSecurityAuditorAware implements AuditorAware<User> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof JwtPrincipal p) {
            // SELECT atmaz, sadece proxy referans üretir
            return Optional.of(em.getReference(User.class, p.userId()));
        }

        return Optional.empty();
    }
}
