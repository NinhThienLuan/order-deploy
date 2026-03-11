package a_and_s_service.compile.infrastructure.cached.redis.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSupported {

    private final StringRedisTemplate stringRedisTemplate;

    public void setKey(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Failed to set key {} in Redis: {}", key, e.getMessage());
        }
    }

    public void setKeyWithTTL(String key, String value, long ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttl));
        } catch (Exception e) {
            log.error("Failed to set key {} with TTL is:{} in Redis: {}", key, ttl, e.getMessage());
        }
    }

    public String getKey(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Failed to get key {} from Redis: {}", key, e.getMessage());
            return null;
        }
    }

    public void deleteKey(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete key {} from Redis: {}", key, e.getMessage());
        }
    }


    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        }
        catch (Exception e) {
            log.error("Failed to check existence of key {} in Redis: {}", key, e.getMessage());
            return false;
        }
    }

    public Long getTTL(String key) {
        try {
            return stringRedisTemplate.getExpire(key);
        } catch (Exception e) {
            log.error("Failed to get TTL for key {} from Redis: {}", key, e.getMessage());
            return null;
        }
    }


}
