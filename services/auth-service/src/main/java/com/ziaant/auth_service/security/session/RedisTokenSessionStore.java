package com.ziaant.auth_service.security.session;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.session-store", havingValue = "redis", matchIfMissing = true)
public class RedisTokenSessionStore implements TokenSessionStore {

    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:access:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void storeRefreshToken(String refreshTokenHash, RefreshSession session, Duration ttl) {
        redisTemplate.opsForValue().set(refreshKey(refreshTokenHash), encode(session), ttl);
    }

    @Override
    public Optional<RefreshSession> consumeRefreshToken(String refreshTokenHash) {
        String key = refreshKey(refreshTokenHash);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        redisTemplate.delete(key);
        return Optional.of(decode(value));
    }

    @Override
    public void revokeRefreshToken(String refreshTokenHash) {
        redisTemplate.delete(refreshKey(refreshTokenHash));
    }

    @Override
    public void blacklistAccessToken(String tokenId, Duration ttl) {
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(blacklistKey(tokenId), "revoked", ttl);
        }
    }

    @Override
    public boolean isAccessTokenBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(tokenId)));
    }

    private String refreshKey(String refreshTokenHash) {
        return REFRESH_PREFIX + refreshTokenHash;
    }

    private String blacklistKey(String tokenId) {
        return BLACKLIST_PREFIX + tokenId;
    }

    private String encode(RefreshSession session) {
        return session.userId() + "|" + session.email();
    }

    private RefreshSession decode(String value) {
        String[] parts = value.split("\\|", 2);
        return new RefreshSession(Long.parseLong(parts[0]), parts[1]);
    }
}
