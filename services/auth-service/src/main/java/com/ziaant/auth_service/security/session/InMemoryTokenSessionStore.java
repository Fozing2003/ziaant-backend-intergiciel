package com.ziaant.auth_service.security.session;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "auth.session-store", havingValue = "memory")
public class InMemoryTokenSessionStore implements TokenSessionStore {

    private final Map<String, ExpiringRefreshSession> refreshTokens = new ConcurrentHashMap<>();
    private final Map<String, Instant> blacklistedAccessTokens = new ConcurrentHashMap<>();

    @Override
    public void storeRefreshToken(String refreshTokenHash, RefreshSession session, Duration ttl) {
        refreshTokens.put(refreshTokenHash, new ExpiringRefreshSession(session, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<RefreshSession> consumeRefreshToken(String refreshTokenHash) {
        ExpiringRefreshSession session = refreshTokens.remove(refreshTokenHash);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(session.session());
    }

    @Override
    public void revokeRefreshToken(String refreshTokenHash) {
        refreshTokens.remove(refreshTokenHash);
    }

    @Override
    public void blacklistAccessToken(String tokenId, Duration ttl) {
        if (!ttl.isNegative() && !ttl.isZero()) {
            blacklistedAccessTokens.put(tokenId, Instant.now().plus(ttl));
        }
    }

    @Override
    public boolean isAccessTokenBlacklisted(String tokenId) {
        Instant expiresAt = blacklistedAccessTokens.get(tokenId);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklistedAccessTokens.remove(tokenId);
            return false;
        }
        return true;
    }

    private record ExpiringRefreshSession(RefreshSession session, Instant expiresAt) {
    }
}
