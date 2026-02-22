package com.dogukan.ecommerce.security;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ActiveUserStore {

    private final StringRedisTemplate redis;

    private static String key(Long userId) {
        return "active-user:" + userId;
    }

    public void markActive(Long userId, Instant tokenExp) {
        long ttlSeconds = Duration.between(Instant.now(), tokenExp).getSeconds();

        // küçük saniye toleransı koymak istedim 5 saniye öncesinden uçsun
        ttlSeconds = Math.max(1, ttlSeconds - 5);
        redis.opsForValue().set(key(userId), userId.toString(), Duration.ofSeconds(ttlSeconds));
    }

    public boolean isActive(Long userId) {
        Boolean exists = redis.hasKey(key(userId));
        System.out.println(exists);
        return Boolean.TRUE.equals(exists);
    }

    public void invalidate(Long userId) {
        redis.delete(key(userId));
    }
}