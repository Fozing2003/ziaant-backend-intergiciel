package com.ziaant.auth_service.security.session;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTokenSessionStoreTest {

    @Test
    void refreshTokenIsConsumedOnlyOnce() {
        InMemoryTokenSessionStore store = new InMemoryTokenSessionStore();

        store.storeRefreshToken("hash", new RefreshSession(1L, "client@test.com"), Duration.ofMinutes(5));

        assertThat(store.consumeRefreshToken("hash")).contains(new RefreshSession(1L, "client@test.com"));
        assertThat(store.consumeRefreshToken("hash")).isEmpty();
    }

    @Test
    void accessTokenBlacklistExpires() throws Exception {
        InMemoryTokenSessionStore store = new InMemoryTokenSessionStore();

        store.blacklistAccessToken("jti-1", Duration.ofMillis(50));

        assertThat(store.isAccessTokenBlacklisted("jti-1")).isTrue();
        Thread.sleep(80);
        assertThat(store.isAccessTokenBlacklisted("jti-1")).isFalse();
    }
}
