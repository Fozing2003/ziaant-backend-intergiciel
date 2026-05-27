package com.ziaant.auth_service.security.session;

import java.time.Duration;
import java.util.Optional;

public interface TokenSessionStore {
    void storeRefreshToken(String refreshTokenHash, RefreshSession session, Duration ttl);

    Optional<RefreshSession> consumeRefreshToken(String refreshTokenHash);

    void revokeRefreshToken(String refreshTokenHash);

    void blacklistAccessToken(String tokenId, Duration ttl);

    boolean isAccessTokenBlacklisted(String tokenId);
}
