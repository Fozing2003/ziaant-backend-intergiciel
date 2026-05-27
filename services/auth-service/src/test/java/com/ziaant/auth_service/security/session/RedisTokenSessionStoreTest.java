package com.ziaant.auth_service.security.session;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RedisTokenSessionStoreTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final RedisTokenSessionStore store = new RedisTokenSessionStore(redisTemplate);

    @Test
    void storesRefreshTokenWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        store.storeRefreshToken("hash", new RefreshSession(42L, "client@test.com"), Duration.ofDays(7));

        verify(valueOperations).set(eq("auth:refresh:hash"), eq("42|client@test.com"), any(Duration.class));
    }

    @Test
    void consumesRefreshTokenAndDeletesIt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:refresh:hash")).thenReturn("42|client@test.com");

        assertThat(store.consumeRefreshToken("hash")).contains(new RefreshSession(42L, "client@test.com"));
        verify(redisTemplate).delete("auth:refresh:hash");
    }

    @Test
    void detectsBlacklistedAccessToken() {
        when(redisTemplate.hasKey("auth:blacklist:access:jti-1")).thenReturn(true);

        assertThat(store.isAccessTokenBlacklisted("jti-1")).isTrue();
    }
}
