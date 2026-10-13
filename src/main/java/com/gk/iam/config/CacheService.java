package com.gk.iam.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CacheService {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // 内存兜底缓存结构: Key -> ExpireValue
    private final Map<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final String value;
        final long expireAtMillis;

        CacheEntry(String value, long timeoutSeconds) {
            this.value = value;
            this.expireAtMillis = timeoutSeconds > 0 ? System.currentTimeMillis() + (timeoutSeconds * 1000) : Long.MAX_VALUE;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAtMillis;
        }
    }

    public void set(String key, String value, long timeoutSeconds) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
                return;
            } catch (Exception e) {
                log.debug("Redis 连接异常，降级使用内存缓存: {}", e.getMessage());
            }
        }
        memoryCache.put(key, new CacheEntry(value, timeoutSeconds));
    }

    public String get(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.debug("Redis 连接异常，降级读取内存缓存: {}", e.getMessage());
            }
        }
        CacheEntry entry = memoryCache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            memoryCache.remove(key);
            return null;
        }
        return entry.value;
    }

    public boolean delete(String key) {
        boolean redisSuccess = false;
        if (redisTemplate != null) {
            try {
                Boolean res = redisTemplate.delete(key);
                redisSuccess = Boolean.TRUE.equals(res);
            } catch (Exception ignored) {
            }
        }
        boolean memSuccess = memoryCache.remove(key) != null;
        return redisSuccess || memSuccess;
    }

    public boolean hasKey(String key) {
        if (redisTemplate != null) {
            try {
                Boolean res = redisTemplate.hasKey(key);
                if (res != null) return res;
            } catch (Exception ignored) {
            }
        }
        CacheEntry entry = memoryCache.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                memoryCache.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }
}

