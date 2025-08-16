package com.job.config.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String key, String value, Long duration) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(duration));
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean isValid(String key, String expectedValue) {
        String value = redisTemplate.opsForValue().get(key);

        return Objects.equals(expectedValue, value);
    }

    public void invalidateAccessToken(String email) {
        delete(email + ":access");
    }

    public void invalidateRefreshToken(String email) {
        delete(email + ":refresh");
    }
}
