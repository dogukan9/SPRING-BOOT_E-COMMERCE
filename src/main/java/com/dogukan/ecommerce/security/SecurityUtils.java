package com.dogukan.ecommerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Long requireCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;

        Object principal = auth.getPrincipal();


        try {
            Method userId = principal.getClass().getMethod("userId");
            Object id = userId.invoke(principal);
            if (id instanceof Long l) return l;
            if (id instanceof Integer i) return i.longValue();
        } catch (Exception ignore) { }

        return null;
    }
}
